package com.example.excel.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
@Component
public class ObjJsonUtil {
    private final ObjectMapper objectMapper;

    // 构造函数注入 ObjectMapper
    public ObjJsonUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 将 JSON 字符串转换为 对象
    public <T> List<T> parseJson(String jsonString, Class<T> clazz) {
        try {
            // 使用 TypeReference 来解析 List<T> 类型
            return objectMapper.readValue(jsonString,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
