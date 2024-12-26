package com.example.excel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.excel.custom.CustomBaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@TableName("t_import_file_log")
@Schema(description = "文件导入日志表")
public class FileImportLog extends CustomBaseEntity {
    private String fileName;
    private String filePath;
    @Schema(description = "文件生成日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime fileDate;
    private String fileDataType;
    private String fileTranslatedType;
    private String sheetName;
    private String md5;
}
