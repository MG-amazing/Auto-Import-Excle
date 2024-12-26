package com.example.excel.custom;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 在插入时，填充 id 字段为随机生成的 UUID
        this.strictInsertFill(metaObject, "id", String.class, UUID.randomUUID().toString().replace("-", ""));
        // 如果需要填充其他字段，可以在这里做类似处理
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 在更新时不做任何填充
    }
}
