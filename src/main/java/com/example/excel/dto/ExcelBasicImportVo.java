package com.example.excel.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExcelBasicImportVo implements Serializable {
    private String excelColumnName;
    private String setFieldDesc;
    private String setFieldName;
    private String excelColumnIndex;
    private String titleFieldId;
}
