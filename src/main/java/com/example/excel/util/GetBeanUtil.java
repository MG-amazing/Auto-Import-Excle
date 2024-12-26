package com.example.excel.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;


@Component
public class GetBeanUtil {


    public GetBeanUtil(ApplicationContext applicationContext, RedisServiceUtil redisServiceUtil) {
        this.applicationContext = applicationContext;
        this.redisServiceUtil = redisServiceUtil;
    }
    private String oldKey="DATA::OLD_ATA";
    private String newKey="DATA::NEW_DATA";
    private String RESULT_KEY="DATA::RESULT_KEY";

    private  final ApplicationContext applicationContext;
    private final RedisServiceUtil redisServiceUtil;


    /**
     * 根据实体类的全限定名动态获取 Service Bean 并调用 saveOrUpdateBatch 调用时saveOrUpdateBatch必须重写一次
     *
     * @param className 实体类全限定名
     * @param entities  要保存的实体列表
     * @param column    去重字段名字符串,分隔
     */
    public Map<String,Object> invokeSaveOrUpdateBatch(String className, List<?> entities,String column,String tenantId) {
        // 保存结果
        Boolean flag = false;
        // 保存数量
        int count = 0;
        Map<String,Object> resultMap = new HashMap<>();
        long id = 0L;

        try {
            // 动态加载实体类
            Class<?> entityClass = Class.forName(className);

            // 获取对应的 Service 实现类 Bean
            String serviceBeanName = getServiceBeanName(entityClass);
            Object serviceBean = applicationContext.getBean(serviceBeanName);
//找方法时解开
//            Method[] methods = serviceBean.getClass().getMethods();
//            for (Method method : methods){
//                System.out.println(method.getName());
//            }
            Class<?> targetClass = getTargetClass(serviceBean);

            Method saveOrUpdateBatch = targetClass.getMethod("saveOrUpdateBatch", Collection.class);
            Method list = targetClass.getMethod("list");
//            RpcContext.getContext().setAttachment(HEADER_TENANT,tenantId);
            Object invoke = list.invoke(serviceBean);
            if (invoke instanceof List&& StrUtil.isNotBlank(column)&& CollUtil.isNotEmpty((List<?>) invoke)){
                id=IdWorker.getId();
                //新数据(首先自身去重)
                List<?> newList = DeduplicateUtil.deduplicate(entities, Arrays.asList(column.split(",")));
                redisServiceUtil.saveListToRedis( newKey+id,newList, Duration.ofMinutes(120));

                //老数据
                List<?> oldList = (List<?>) invoke;
                redisServiceUtil.saveListToRedis( oldKey+id,oldList, Duration.ofMinutes(5));
                //去重
                List<?> deduplicate = redisServiceUtil.deduplicate(oldKey + id, newKey + id, column, entityClass);
                //提取

                //保存
                flag = (Boolean) saveOrUpdateBatch.invoke(serviceBean, deduplicate);
                count = deduplicate.size();


            }else {
                if (StrUtil.isNotBlank(column)){
                //新数据(首先自身去重)
                List<?> newList = DeduplicateUtil.deduplicate(entities, Arrays.asList(column.split(",")));
                //保存
                    flag = (Boolean) saveOrUpdateBatch.invoke(serviceBean, newList);
                    count = newList.size();}
                else {
                    flag = (Boolean) saveOrUpdateBatch.invoke(serviceBean, entities);
                    count = entities.size();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("flag", false);
            resultMap.put("count", 0);
            return resultMap;
        }
        resultMap.put("flag", flag);
        resultMap.put("count", count);
        resultMap.put("threadId", id);
        return resultMap;
    }

    /**
     * 获取目标类，解决 CGLIB 动态代理类问题
     *
     * @param proxy 代理对象
     * @return 目标类的 Class
     */
    private Class<?> getTargetClass(Object proxy) {
        if (AopUtils.isAopProxy(proxy)) {
            return AopProxyUtils.ultimateTargetClass(proxy); // 获取最终目标类
        }
        return proxy.getClass();
    }
    /**
     * 根据实体类动态推导 Service Bean 名称
     *
     * @param entityClass 实体类
     * @return Service Bean 名称
     */
    private static String getServiceBeanName(Class<?> entityClass) {
        // 假设实现类名称规则为：实体类短名称 + ServiceImpl，首字母小写
        String entityName = entityClass.getSimpleName();
        String serviceName = entityName + "ServiceImpl";
        return Character.toLowerCase(serviceName.charAt(0)) + serviceName.substring(1);
    }
}