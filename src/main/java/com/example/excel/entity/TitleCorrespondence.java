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
@TableName("t_title_correspondence")
@Schema(description = "字段-4级")
public class TitleCorrespondence extends CustomBaseEntity {
    private String titleFieldId;//字段的id
    private String excelColumnName ;//名字
    private String titleMenuId;//三级id
    private String nameField;//字段
    private String titleMenuName;//二级名称
    private String type;//类型
    private String nameEntity;//实体类
    private String setFieldDesc;//对应名字
    @TableField(exist = false)
    private String threeFormId;
    @TableField(exist = false)
    private String twoFormId;
    @TableField(exist = false)
    private String secondFormId;

}