package com.example.excel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.excel.custom.CustomBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@TableName("t_title_type")
@Schema(description = "类型-3级")
public class TitleType extends CustomBaseEntity {
    private String titleMenuId;
    private String name;
    private String fieldIds;
    @TableField(exist = false)
    private String secondFormId;//二级类型查询条件
    @TableField(exist = false)
    private String twoFormId;//二级类型查询条件
    @TableField(exist = false)
    private List<TitleFields> dataList;//三级类型查询条件
}