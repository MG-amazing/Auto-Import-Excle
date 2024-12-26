package com.example.excel.util;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 去重通用工具类
 */
public class DeduplicateUtil {

    /**
     * 根据动态字段去重
     *
     * @param list        原始数据列表
     * @param fieldNames  动态字段名列表，用于去重
     * @param <T>         列表中数据的类型
     * @return 去重后的列表
     */
    public static <T> List<T> deduplicate(List<T> list, List<String> fieldNames) {
        return new ArrayList<>(list.stream()
                .collect(Collectors.toMap(
                        item -> generateKey(item, fieldNames), // 生成唯一键
                        Function.identity(), // 保留原始对象
                        (existing, replacement) -> replacement // 如果重复，用后来的值替换
                ))
                .values());
    }
    /**
     * 根据字段名列表动态生成唯一键
     *
     * @param item       数据对象
     * @param fieldNames 字段名列表
     * @param <T>        数据对象的类型
     * @return 生成的唯一键
     */
    private static <T> String generateKey(T item, List<String> fieldNames) {
        StringBuilder keyBuilder = new StringBuilder();
        for (String fieldName : fieldNames) {
            try {
                Field field = item.getClass().getDeclaredField(fieldName);
                field.setAccessible(true); // 允许访问私有字段
                Object value = field.get(item);
                keyBuilder.append(value).append("|"); // 用“|”分隔字段值
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("无法访问字段：" + fieldName, e);
            }
        }
        return keyBuilder.toString();
    }

    public static void main(String[] args) {
        List<String> list = Arrays.asList("name", "date");
        List<Dome>one=new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        one.add(new Dome("牛马",now));
        one.add(new Dome("牛马",now));
        one.add(new Dome("畜生",now));
        one.add(new Dome("畜生",now));
        one.add(new Dome("畜生",LocalDateTime.MAX));
        List<Dome> deduplicate = DeduplicateUtil.deduplicate(one, list);
        System.out.println(deduplicate);
    }
}
class Dome{
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime date;

    public Dome(String name, LocalDateTime date) {
        this.name = name;
        this.date = date;
    }
    public Dome() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Dome{" +
                "name='" + name + '\'' +
                ", date=" + date +
                '}';
    }
}
