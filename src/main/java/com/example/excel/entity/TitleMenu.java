package com.example.excel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.excel.custom.CustomBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@TableName("t_title_menu")
@Schema(description = "菜单-2级")
public class TitleMenu extends CustomBaseEntity {
    private String titleMajorId;
    private String name;
    private String entity;
    @TableField(exist = false)
    private String oneFormId;//一级菜单查询id（查询条件）
}
