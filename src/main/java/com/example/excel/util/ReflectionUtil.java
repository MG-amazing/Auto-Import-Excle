package com.example.excel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtil {

    // 缓存类的getter和setter方法
    private static final Map<Class<?>, Map<String, Method>> getterCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Method>> setterCache = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

    /**
     * 根据字段名动态获取对象的属性值（通过get方法）
     *
     * @param obj       目标对象
     * @param fieldName 字段名
     * @return 字段值
     * @throws Exception 如果字段名无效或反射失败抛出异常
     */
    public static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Class<?> clazz = obj.getClass();
        // 从缓存中获取getter方法
        Map<String, Method> getters = getterCache.computeIfAbsent(clazz, ReflectionUtil::cacheGetters);

        // 获取方法并调用
        Method method = getters.get(fieldName);
        if (method == null) {
            throw new NoSuchMethodException("Getter not found for field: " + fieldName);
        }
        return method.invoke(obj);
    }

    /**
     * 根据字段名动态设置对象的属性值（通过set方法）
     *
     * @param obj       目标对象
     * @param fieldName 字段名
     * @param value     要设置的字段值
     * @throws Exception 如果字段名无效或反射失败抛出异常
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        // 从缓存中获取setter方法
        Map<String, Method> setters = setterCache.computeIfAbsent(clazz, ReflectionUtil::cacheSetters);

        // 获取方法并调用
        Method method = setters.get(fieldName);
        if (method == null) {
            throw new NoSuchMethodException("Setter not found for field: " + fieldName);
        }

        // 类型转换处理（可选）
        Object convertedValue = convertType(method.getParameterTypes()[0], value);
        method.invoke(obj, convertedValue);
    }

    /**
     * 缓存getter方法
     *
     * @param clazz 类
     * @return 字段名到getter方法的映射
     */
    private static Map<String, Method> cacheGetters(Class<?> clazz) {
        Map<String, Method> getters = new ConcurrentHashMap<>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                String fieldName = decapitalizeFirstLetter(method.getName().substring(3));
                getters.put(fieldName, method);
            }
        }
        return getters;
    }

    /**
     * 缓存setter方法
     *
     * @param clazz 类
     * @return 字段名到setter方法的映射
     */
    private static Map<String, Method> cacheSetters(Class<?> clazz) {
        Map<String, Method> setters = new ConcurrentHashMap<>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                String fieldName = decapitalizeFirstLetter(method.getName().substring(3));
                setters.put(fieldName, method);
            }
        }
        return setters;
    }

    /**
     * 字符串首字母大写
     */
    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 字符串首字母小写
     */
    private static String decapitalizeFirstLetter(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 类型转换方法（根据需求实现）
     */
    private static Object convertType(Class<?> targetType, Object value) {
        if (value == null || value.toString().trim().isEmpty()) {
            return null;  // 处理空值
        }
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value.toString());
        }
        if (targetType == String.class) {
            return value.toString();
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value.toString());
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value.toString());
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(value.toString());
        }
        if (targetType == LocalDateTime.class) {
            return convertLocalDateTime(value.toString());
        }
        if (targetType == LocalDate.class) {
            return LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        // 其他类型转换...
        return value;
    }

    private static Object convertLocalDateTime(String value) {
        // 定义多个可能的输入格式
        List<String> possiblePatterns = Arrays.asList(
                "yyyy/M/d HH'点'mm'分'ss'秒'", // 格式一：2018/7/26 08点12分16秒
                "yyyy/M/d H'点'mm'分'ss'秒'", // 格式二：2018/7/6 8点12分16秒
                "yyyy/MM/dd HH'点'mm'分'ss'秒'", // 格式三：2018/07/26 08点12分16秒
                "yyyy/MM/dd H'点'mm'分'ss'秒'", // 格式四：2018/07/26 8点12分16秒
                "yyyy/M/d H'点'mm'分'", // 格式五：2018/07/26 8点12分
                "yyyy/M/d HH'点'mm'分'", // 格式六：2018/07/26 08点12分
                "yyyy-MM-dd HH:mm:ss",          // 格式七：2018-07-26 08:12:16
                "yyyy-MM-dd H:mm:ss",          // 格式八：2018-07-26 8:02:06
                "yyyy-MM-dd HH:mm",          // 格式九：2018-07-26 08:12
                "yyyy-MM-dd H:mm",          // 格式十：2018-07-26 8:02
                "yyyy/MM/dd HH:mm:ss",          // 格式十一：2018/07/26 08:12:16
                "yyyy/MM/dd H:mm:ss",          // 格式十二：2018/07/26 8:02:06
                "yyyy/MM/dd HH:mm",          // 格式十三：2018/07/26 08:12
                "yyyy/MM/dd H:mm",         // 格式十四：2018/07/26 8:02
                "yyyy-M-d HH:mm",          // 格式十五：2018-7-6 08:12
                "yyyy-MM-d HH:mm",          // 格式十六：2018-07-6 08:12
                "yyyy-M-d HH:mm",          // 格式十七：2018-7-6 08:12
                "yyyy-M-dd H:mm",          // 格式十八：2018-7-06 8:12
                "yyyy-M-d H:mm",          // 格式十八：2018-7-6 8:12
                "yyyy-M-dd HH:mm"          // 格式十九：2018-7-06 08:12
        );

        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (String pattern : possiblePatterns) {
            try {
                // 尝试用当前模式解析输入
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime dateTime = LocalDateTime.parse(value, formatter);

                // 成功解析后，按照目标格式输出
                return LocalDateTime.parse(dateTime.format(targetFormatter), targetFormatter);
            } catch (Exception e) {
                log.info("尝试使用格式 '" + pattern + "' 解析失败：" +value+"错误信息："+ e.getMessage());
            }
        }
        throw new IllegalArgumentException("Invalid date format: " + value);
    }
}
