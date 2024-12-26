package com.example.excel.util;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class RedisServiceUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceUtil.class);
//    @Value(value = "${redis.overTime}")
    private Integer overTime;

    public void init() {
        if (this.overTime == null ||this.overTime==0) {
            this.overTime = 2;
        }
    }

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper ;

     public RedisServiceUtil(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 确认key是否存在
     *
     * @param key 键
     * @return 如果键对应的值为空，返回true；否则返回false
     */
    public boolean isValueEmpty(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return value == null || value.isEmpty();
    }

    /**
     * 删除key
     *
     * @param key 键
     * @return 如果删除成功，返回true；否则返回false
     */
    public boolean deleteKey(String key) {
        Boolean result = stringRedisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 模糊删除key
     *
     * @param pattern 模糊匹配模式
     * @return 如果删除成功，返回true；否则返回false
     */
    public boolean deleteKeysWithPattern(String pattern) {
        if (StrUtil.isNotBlank(pattern)) {
            if (!pattern.contains("*")) {
                pattern += "*";
            }
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deletedCount = stringRedisTemplate.delete(keys);
                return deletedCount != null && deletedCount > 0;
            }
        }
        return false;
    }

    /**
     * 将对象序列化为JSON字符串并存入Redis
     *
     * @param key        键
     * @param object     要存储的对象
     * @param version    版本号
     * @param expiration 过期时间
     * @param <T>        对象类型
     * @return 是否存储成功
     */
    public <T> boolean saveObjectVersion(String key, T object, int version, Duration expiration) {
        String fullKey = key + ":" + version;
        try {
            String value = objectMapper.writeValueAsString(object);
            stringRedisTemplate.opsForValue().set(fullKey, value, expiration);
            return true;
        } catch (Exception e) {
            logger.error("Error saving object to Redis with key: {}", fullKey, e);
            return false;
        }
    }

    /**
     * 将集合序列化为JSON字符串并存入Redis
     *
     * @param key        键
     * @param collection 要存储的集合
     * @param expiration 过期时间
     * @param <T>        对象类型
     * @return 是否存储成功
     */
    public <T> boolean saveListVersion(String key, Collection<T> collection, Duration expiration) {

        try {
            String value = objectMapper.writeValueAsString(collection);
            stringRedisTemplate.opsForValue().set(key, value, expiration);
            return true;
        } catch (Exception e) {
            logger.error("Error saving list to Redis with key: {}", key, e);
            return false;
        }
    }

    /**
     * 将Map序列化为JSON字符串并存入Redis
     *
     * @param key        键
     * @param map        要存储的Map
     * @param version    版本号
     * @param expiration 过期时间
     * @return 是否存储成功
     */
    public boolean saveMapVersion(String key, Map<String, String> map, int version, Duration expiration) {
        String fullKey = key + ":" + version;
        try {
            String value = objectMapper.writeValueAsString(map);
            stringRedisTemplate.opsForValue().set(fullKey, value, expiration);
            return true;
        } catch (Exception e) {
            logger.error("Error saving map to Redis with key: {}", fullKey, e);
            return false;
        }
    }

    public Map<String, String> getMapVersion(String key, int version) {
        String fullKey = key + ":" + version;
        try {
            String value = stringRedisTemplate.opsForValue().get(fullKey);
            if (value != null) {
                return objectMapper.readValue(value, new TypeReference<Map<String, String>>() {});
            }
        } catch (Exception e) {
            logger.error("Error retrieving map from Redis with key: {}", fullKey, e);
        }
        return null;
    }

    /**
     * Map<String, List<Object>> map只支持
     * @param key
     * @param map
     * @param version
     * @param expiration
     * @return 是否存储成功
     * @param <T>
     */

    public <T> boolean saveMapObjectVersion(String key, Map<String, List<T>> map, int version, Duration expiration) {
        String fullKey = key + ":" + version;
        try {
            String value = objectMapper.writeValueAsString(map);
            stringRedisTemplate.opsForValue().set(fullKey, value, expiration);
            return true;
        } catch (Exception e) {
            logger.error("Error saving map to Redis with key: {}", fullKey, e);
            return false;
        }
    }

    /**
     *  Map<String, List<Object>>返回值
     * @param key
     * @param version
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> Map<String, List<T>> getMapObjectVersion(String key, int version, Class<T> clazz) {
        String fullKey = key + ":" + version;
        try {
            String value = stringRedisTemplate.opsForValue().get(fullKey);
            if (value != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                // 构造 Map<String, List<T>> 类型
                JavaType mapType = objectMapper.getTypeFactory().constructMapType(
                        Map.class,
                        objectMapper.getTypeFactory().constructType(String.class),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
                );
                return objectMapper.readValue(value, mapType);
            }
        } catch (Exception e) {
            logger.error("Error retrieving map from Redis with key: {}", fullKey, e);
        }
        return null;
    }






    /**
     * 从Redis获取值并反序列化为指定类型的对象
     *
     * @param key   键
     * @param clazz 要反序列化的类型
     * @param <T>   对象类型
     * @return 反序列化后的对象，如果失败返回null
     */
    public <T> Optional<T> getValue(String key, Class<T> clazz) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            try {
                return Optional.of(objectMapper.readValue(value, clazz));
            } catch (IOException e) {
                logger.error("Error deserializing value from Redis with key: {}", key, e);
            }
        }
        return Optional.empty();
    }

    /**
     * 从Redis获取值并反序列化为指定类型的对象列表
     *
     * @param key   键
     * @param clazz 要反序列化的类型
     * @param <T>   对象类型
     * @return 反序列化后的对象列表，如果失败返回空列表
     */
    public <T> List<T> getValueList(String key, Class<T> clazz) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value != null) {
            try {
                return objectMapper.readValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            } catch (IOException e) {
                logger.error("Error deserializing value list from Redis with key: {}", key, e);
            }
        }
        return Collections.emptyList();
    }
    /**
     * 将传入的 List<T> 转换为 JSON 格式后存入 Redis，并设置过期时间
     *
     * @param key        Redis 存储的键
     * @param list       要存储的对象列表
     * @param expiration 过期时间
     * @param <T>        泛型类型
     */
    public <T> void saveListToRedis(String key, List<T> list, Duration expiration) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<String>> futureList = new ArrayList<>();
        try {
            // 多线程并行处理 JSON 序列化
            for (T item : list) {
                futureList.add(executor.submit(() -> objectMapper.writeValueAsString(item)));
            }

            // 主线程汇总结果
            List<String> jsonList = new ArrayList<>();
            for (Future<String> future : futureList) {
                jsonList.add(future.get());
            }

            // 单线程写入 Redis
            stringRedisTemplate.opsForList().rightPushAll(key, jsonList);
            stringRedisTemplate.expire(key, expiration);

        } catch (Exception e) {
            throw new RuntimeException("操作失败: " + e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }


    /** 互斥锁
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setRedisLock(String key,String value,Integer time) {
        return BooleanUtil.isTrue(stringRedisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(time)));
    }
    /**
     * 执行去重操作
     * @param oldSetKey Redis 中旧集合的键名
     * @param newSetKey Redis 中新集合的键名
     * @param fields 需要去重的字段列表，用逗号分隔
     * @return 去重后的新集合
     */
    public <T> List<T> deduplicate(String oldSetKey, String newSetKey, String fields, Class<T> clazz) {
        return stringRedisTemplate.execute((RedisConnection connection) -> {
            // 加载 Lua 脚本
            Resource resource = new ClassPathResource("lua/deduplicate.lua");
            byte[] script = null;
            try {
                script = Files.readAllBytes(resource.getFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read Lua script", e);
            }

            // 序列化键和参数
            byte[] oldSetKeyBytes = stringRedisTemplate.getStringSerializer().serialize(oldSetKey);
            byte[] newSetKeyBytes = stringRedisTemplate.getStringSerializer().serialize(newSetKey);
            byte[] fieldsBytes = stringRedisTemplate.getStringSerializer().serialize(fields);

            // 执行 Redis Lua 脚本
            List<byte[]> result = connection.eval(
                    script,                // Lua 脚本
                    ReturnType.MULTI,      // 返回类型
                    2,                     // 键数量
                    oldSetKeyBytes, newSetKeyBytes, // 键
                    fieldsBytes            // 参数
            );

            // 转换返回值为 List<T>
            List<T> uniqueObjects = new ArrayList<>();
            if (result != null) {
                for (byte[] item : result) {
                    String jsonString = stringRedisTemplate.getStringSerializer().deserialize(item);
                    if (jsonString != null) {
                        // 使用 Jackson 或 Gson 将 JSON 字符串转换为 T
                        T object = parseJson(jsonString, clazz);
                        if (object != null) {
                            uniqueObjects.add(object);
                        }
                    }
                }
            }
            return uniqueObjects;
        });
    }

    // JSON 解析工具方法
    private <T> T parseJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON: " + json, e);
        }
    }





}



