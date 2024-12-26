package com.example.excel.util;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FiledUtil {


    /**
     * 从多个实体类中提取数据，返回一个大 Map。
     * @param classes 多个实体类的 Class 对象
     * @return Map<类的描述, Map<字段名称, 字段描述>>
     */
    public static Map<String, Map<String, String>> extractFromEntities(Class<?>... classes) {
        Map<String, Map<String, String>> result = new HashMap<>();

        for (Class<?> clazz : classes) {
            // 获取类上的 @Schema 注解
            Schema classSchema = clazz.getAnnotation(Schema.class);
            if (classSchema == null || classSchema.description().isEmpty()) {
                continue; // 如果类没有 @Schema 或描述为空，则跳过
            }
            String classDescription = classSchema.description();

            // 提取该类的字段信息
            Map<String, String> fieldMap = extractFields(clazz);
            result.put(classDescription, fieldMap);
        }

        return result;
    }

    /**
     * 从单个实体类中提取字段名称及其描述。
     * @param clazz 实体类的 Class 对象
     * @return Map<字段名称, 字段描述>
     */
    private static Map<String, String> extractFields(Class<?> clazz) {
        Map<String, String> fieldMap = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // 检查字段是否带有 @TableField(exist = false) 注解
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null && !tableField.exist()) {
                continue; // 忽略带有 exist=false 的字段
            }

            // 获取字段的 @Schema 注解
            Schema schema = field.getAnnotation(Schema.class);
            String description = schema != null ? schema.description() : "无描述";

            // 将字段名称及描述放入 Map
            fieldMap.put(field.getName(), description);
        }
        fieldMap.put("className",ClassObjUtil.getClassName(clazz));
        return fieldMap;
    }

    public static void main(String[] args) {
//        Map<String, Map<String, String>> stringMapMap = FiledUtil.extractFromEntities(PhoneNumberLib.class, BankAccountLib.class, AlipayAccountLib.class, WechatAccountLib.class, BaseAddressLib.class,TransactionIdentifierLib.class,StockCodeLib.class);

//        System.out.println(stringMapMap);

    }
}
