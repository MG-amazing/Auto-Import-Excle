package com.example.excel.util;

import com.example.excel.entity.FileImportLog;
import com.example.excel.service.FileImportLogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FileImportLogServiceUtil {

    @Value("${file.save.path}") // 从配置文件读取文件保存路径
    private String fileSavePath;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final FileImportLogService fileImportLogService;
    public final ObjectMapper objectMapper;

    public FileImportLogServiceUtil(FileImportLogService fileImportLogService, ObjectMapper objectMapper) {
        this.fileImportLogService = fileImportLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据规则生成文件路径
     *
     * @param originalFileName 原始文件名
     * @param sheetName
     * @return 生成的文件路径
     */
    private String generateFilePath(MultipartFile originalFileName, String sheetName) {
        // 获取当前日期作为文件夹名称
        String dateFolder = DATE_FORMAT.format(new Date());

        // 计算文件名的 SHA256 值
        String id = FileMd5Utils.getFileMd5(originalFileName);


        // 拼接最终文件名
        String finalFileName = id + "_" + originalFileName.getOriginalFilename() + "_" + sheetName;
        String s = fileSavePath + File.separator + dateFolder + File.separator + finalFileName + ".json";
//        if(CollUtil.isNotEmpty(fileImportLogService.list(new LambdaQueryWrapper<FileImportLog>().like(FileImportLog::getFilePath, finalFileName)))){
//            throw new RuntimeException("该文件已经导入过");
//        }
        // 构建完整路径
        return s;
    }


    /**
     * @param orgFile
     * @param object
     */
    public String writeToFile(MultipartFile orgFile, String sheetName, Object object) {
        String filePath = generateFilePath(orgFile, sheetName);

        // 确保目录存在
        File file = new File(filePath);
        File directory = file.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("创建目录失败");
        }

        // 记录文件导入日志
        FileImportLog fileImportLog = new FileImportLog();
        fileImportLog.setFileName(orgFile.getOriginalFilename());
        fileImportLog.setFilePath(filePath);
        fileImportLog.setFileDate(LocalDateTime.now());
        fileImportLog.setFileDataType(object.getClass().getName());
        fileImportLog.setMd5(FileMd5Utils.getFileMd5(orgFile));
        fileImportLog.setSheetName(sheetName);
        fileImportLogService.save(fileImportLog);

        // 将对象写入文件
        try {
            objectMapper.writeValue(file, object);
            System.out.println("对象已成功写入文件: " + filePath);
        } catch (IOException e) {
            System.err.println("写入文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return filePath;
    }

    /**
     * 生成文件路径，假设这里是将文件保存到特定的文件夹
     */


    /**
     * 从文件读取对象
     *
     * @param filePath 文件路径
     * @return 读取到的对象
     */

    public List<Map<Integer, String>> readFromFile(String filePath) {
        List<Map<Integer, String>> result = null;
        try {
            // Use TypeReference for better type safety and readability
            return objectMapper.readValue(
                    new File(filePath),
                    new TypeReference<List<Map<Integer, String>>>() {
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static void main(String[] args) {


    }
}
