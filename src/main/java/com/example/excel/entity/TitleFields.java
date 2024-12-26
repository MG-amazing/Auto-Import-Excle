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
@TableName("t_title_field")
@Schema(description = "字段-3级")
public class TitleFields extends CustomBaseEntity {
    private String titleMenuId;
    private String type;
    private String name;
    private String nameField;
    private String menuName;
    private String isDeduplicate;
    private String isExtract;
    private String deDefault;
    private String exDefault;


    @TableField(exist = false)
    private String twoFormId;//二级菜单id
    @TableField(exist = false)
    private String secondFormId;//类型菜单id
    @TableField(exist = false)
    private String isGetEntity;//二级菜单id
    @TableField(exist = false)
    private String nameType;
    @TableField(exist = false)
    private String isGetType;
}
