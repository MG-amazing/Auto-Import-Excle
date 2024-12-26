package com.example.excel.dto;

import lombok.Data;

import java.util.List;

@Data
public class TitleImportDataDto {
    private List<ExcelBasicImportVo> titleList;
    private String sheetName;
    private String filePath;
    private Integer titleRow;
    private String dataTableEntity;
    private String titleRowSourceId;
    private String dataTableId;
    private String dataPartId;
    private String tenantId;
    private String isDe;




}
