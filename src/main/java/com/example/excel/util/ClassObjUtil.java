package com.example.excel.util;

import java.util.Objects;

public class ClassObjUtil {

    private static final String BASE_PACKAGE = "com.vt.api";

    /**
     * 获取类的全限定名
     * @param clazz 类对象
     * @return 类的全限定名，如果类为 null 则返回 null
     */
    public static String getClassName(Class<?> clazz) {
        return clazz != null ? clazz.getName() : null;
    }

    /**
     * 根据类的全限定名加载并创建对象，仅允许加载 com.vt.api 包下的类
     * @param fullClassName 类的全限定名
     * @return 动态创建的对象
     * @throws IllegalArgumentException 如果类不属于 com.vt.api 包
     * @throws Exception 其他异常如类加载失败、无无参构造器等
     */
    public static Object createObject(String fullClassName) throws Exception {
        // 判断是否在允许的包下
        if (Objects.isNull(fullClassName) || !fullClassName.startsWith(BASE_PACKAGE)) {
            throw new IllegalArgumentException("只能加载 " + BASE_PACKAGE + " 包下的类！");
        }

        // 加载类
        Class<?> clazz = Class.forName(fullClassName);

        // 创建对象（要求有无参构造器）
        return clazz.getDeclaredConstructor().newInstance();
    }
}
