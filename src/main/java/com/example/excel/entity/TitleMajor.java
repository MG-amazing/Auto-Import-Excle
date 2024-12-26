package com.example.excel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.excel.custom.CustomBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@TableName("t_title_major")
@Schema(description = "主表标题-1级")
public class TitleMajor extends CustomBaseEntity {
    private String name;
}
