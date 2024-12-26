package com.example.excel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.excel.custom.CustomBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_title_identify_lib")
@Data
@Schema(description = "案件数据预览")
public class TitleIdentify extends CustomBaseEntity {

    @Schema(description = "标题行识别名称")
    private String titleIdentifyName;


    @Schema(description = "一级菜单id")
    private String oneFormId;
    @Schema(description = "一级菜单名称")
    private String oneFormName;

    @Schema(description = "二级菜单id")
    private String twoFormId;
    @Schema(description = "二级菜单名称")
    private String twoFormName;
    @Schema(description = "实体类全量名")
    private String entity;


    @Schema(description = "标题行")
    private String titleRow;
    @Schema(description = "标题行号")
    private String rowNumber;


    @Schema(description = "结束标志")
    private String endFlag;

    @Schema(description = "是否系统字段")
    private String isSystem;
    @Schema(description = "标题名对应字段（Json）Map《String,String》")
    private String fieldMap;
    @Schema(description = "1.手动添加2.导入时用户自定义3.默认模板")
    private String type;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}