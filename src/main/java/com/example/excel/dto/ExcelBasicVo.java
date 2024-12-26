package com.example.excel.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ExcelBasicVo implements Serializable {
    private List<ExcelBasicImportVo> titleList;
    private List<Map<Integer, String>> data;
    private String sheetName;
    private String filePath;
    private Integer titleRow;
    private String dataTableName;
    private String dataTableEntity;
    private String dataTableId;
    private String dataPartId;
    private Integer dataSize;
    private String titleRowSourceId;
    private String tenantId;

}