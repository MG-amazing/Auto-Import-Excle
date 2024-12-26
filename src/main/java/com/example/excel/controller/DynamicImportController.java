package com.example.excel.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import com.example.excel.common.Result;
import com.example.excel.common.WebConstant;
import com.example.excel.dto.ExcelBasicImportVo;
import com.example.excel.dto.ExcelBasicVo;
import com.example.excel.entity.FileImportLog;
import com.example.excel.entity.TitleCorrespondence;
import com.example.excel.entity.TitleIdentify;
import com.example.excel.service.FileImportLogService;
import com.example.excel.service.TitleCorrespondenceService;
import com.example.excel.service.TitleIdentifyService;
import com.example.excel.service.impl.FileImportLogServiceImpl;
import com.example.excel.util.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class DynamicImportController {
    private final String PATH = WebConstant.API_PATH + "/dynamic_import";



    private final TitleIdentifyService titleIdentifyService;
    private final TitleCorrespondenceService titleCorrespondenceService;


    private final GetBeanUtil getBeanUtil;
    private final FileImportLogService fileImportLogService;



    private final FileImportLogServiceUtil fileImportLogServiceUtil;

    public DynamicImportController(TitleIdentifyService titleIdentifyService, TitleCorrespondenceService titleCorrespondenceService, GetBeanUtil getBeanUtil, FileImportLogService fileImportLogService, FileImportLogServiceUtil fileImportLogServiceUtil) {
        this.titleIdentifyService = titleIdentifyService;
        this.titleCorrespondenceService = titleCorrespondenceService;
        this.getBeanUtil = getBeanUtil;
        this.fileImportLogService = fileImportLogService;
        this.fileImportLogServiceUtil = fileImportLogServiceUtil;
    }


    @PostMapping(PATH + "/wash")

    public Result<?> replace(@RequestParam MultipartFile file) {
        List<ExcelBasicVo> dataList = new ArrayList<>();
        //判断之前是否上传过该文件
        List<FileImportLog> list = fileImportLogService.list(new LambdaQueryWrapper<FileImportLog>().eq(FileImportLog::getMd5, FileMd5Utils.getFileMd5(file)));
        if (CollUtil.isEmpty(list)) {

            List<ExcelImportUtils.Sheet> sheets = ExcelImportUtils.readExcel(file, true);
            //遍历每一个sheet单独处理保存
            sheets.forEach(d -> {
                //获取sheet页里所有数据
                List<Map<Integer, String>> rows = d.getRows();
                //解析标题行的逻辑
                int titleRow = 0;
                Map<String, Object> rowData = findTitleRow(rows);
                titleRow = (int) rowData.get("number");

                Map<Integer, String> data = rows.get(titleRow);
                List<Map<Integer, String>> filteredRows;
//                if (titleRow != 0) {
//                    //截取至表头
//                    filteredRows = rows.subList(titleRow, rows.size());
//
//                } else {
                filteredRows = rows;
//                }
                //转换成json 存储到文件里
                String filePath = fileImportLogServiceUtil.writeToFile(file, d.getSheetName(), filteredRows);

                int finalTitleRow = titleRow;
                List<ExcelBasicImportVo> finalTitleList = new ArrayList<>();

                ExcelBasicVo excelBasicVo = new ExcelBasicVo();
                if (ObjectUtil.isNotEmpty(rowData.get("titleIdentify"))) {
                    TitleIdentify titleIdentify = (TitleIdentify) rowData.get("titleIdentify");
                    excelBasicVo.setDataTableId(titleIdentify.getTwoFormId());
                    excelBasicVo.setDataTableName(titleIdentify.getTwoFormName());
                    excelBasicVo.setDataTableEntity(titleIdentify.getEntity());
                    excelBasicVo.setDataPartId(titleIdentify.getOneFormId());
                    //获取表头配置
                    finalTitleList = getTitleConfig(data, titleIdentify.getTwoFormId());
                } else {
                    //获取表头配置
                    finalTitleList = getTitleConfig(data, "");
                    excelBasicVo.setDataTableId("");
                    excelBasicVo.setDataTableName("");
                    excelBasicVo.setDataTableEntity("");
                    excelBasicVo.setDataPartId("");
                }
                excelBasicVo.setDataSize(filteredRows.size());
                //截取前100条
                if (filteredRows.size() > 100) {
                    filteredRows = filteredRows.subList(0, 100);
                }
                excelBasicVo.setTitleList(finalTitleList);
                excelBasicVo.setData(filteredRows);
                excelBasicVo.setFilePath(filePath);
                excelBasicVo.setTitleRow(titleRow);
                excelBasicVo.setSheetName(d.getSheetName());
                excelBasicVo.setTitleRowSourceId(rowData.get("titleRowSourceId")==null?null:String.valueOf(rowData.get("titleRowSourceId")));
                dataList.add(excelBasicVo);


            });
        } else {
            List<String> filePaths = list.stream().map(FileImportLog::getFilePath).collect(Collectors.toList());
            List<ExcelImportUtils.Sheet> sheets = new ArrayList<>();
            List<String> sheetNames = list.stream().map(FileImportLog::getSheetName).collect(Collectors.toList());
            for (int i = 0; i < filePaths.size(); i++) {
                List<Map<Integer, String>> lists = fileImportLogServiceUtil.readFromFile(filePaths.get(i));
                sheets.add(new ExcelImportUtils.Sheet(sheetNames.get(i), lists));
            }
            for (int i = 0; i < sheets.size(); i++) {
                ExcelImportUtils.Sheet d = sheets.get(i);
                List<Map<Integer, String>> rows = d.getRows();
                //解析标题行的逻辑
                int titleRow = 0;

                Map<String, Object> rowData = findTitleRow(rows);
                titleRow = (int) rowData.get("number");

                Map<Integer, String> data = rows.get(titleRow);
                List<Map<Integer, String>> filteredRows;
//                if (titleRow != 0) {
//                    //截取至表头
//                    filteredRows = rows.subList(titleRow, rows.size());
//
//                } else {
                filteredRows = rows;
//                }

                int finalTitleRow = titleRow;
                List<ExcelBasicImportVo> finalTitleList = new ArrayList<>();
                ExcelBasicVo excelBasicVo = new ExcelBasicVo();
                if (ObjectUtil.isNotEmpty(rowData.get("titleIdentify"))) {
                    TitleIdentify titleIdentify = (TitleIdentify) rowData.get("titleIdentify");
                    excelBasicVo.setDataTableId(titleIdentify.getTwoFormId());
                    excelBasicVo.setDataTableName(titleIdentify.getTwoFormName());
                    excelBasicVo.setDataTableEntity(titleIdentify.getEntity());
                    excelBasicVo.setDataPartId(titleIdentify.getOneFormId());
                    //获取表头配置
                    finalTitleList = getTitleConfig(data, titleIdentify.getTwoFormId());
                } else {
                    //获取表头配置
                    finalTitleList = getTitleConfig(data, "");
                    excelBasicVo.setDataTableId("");
                    excelBasicVo.setDataTableName("");
                    excelBasicVo.setDataTableEntity("");
                    excelBasicVo.setDataPartId("");
                }
                excelBasicVo.setTitleList(finalTitleList);
                excelBasicVo.setData(filteredRows);
                excelBasicVo.setFilePath(filePaths.get(i));
                excelBasicVo.setTitleRow(titleRow);
                excelBasicVo.setSheetName(d.getSheetName());
                excelBasicVo.setDataSize(filteredRows.size());
                //截取前100条
                if (filteredRows.size() > 100) {
                    filteredRows = filteredRows.subList(0, 100);
                }
                excelBasicVo.setData(filteredRows);
                excelBasicVo.setTitleRowSourceId(rowData.get("titleRowSourceId")==null?null:String.valueOf(rowData.get("titleRowSourceId")));

                dataList.add(excelBasicVo);
            }

        }


        return Result.OK(dataList);
    }

    /**
     * 判断并返回标题行的索引
     *
     * @param rows Excel 中的所有行数据
     * @return 标题行的索引，如果没有找到则返回 -1
     */
    public Map<String, Object> findTitleRow(List<Map<Integer, String>> rows) {
        Map<String, Object> result = new HashMap<>();
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String collect = rows.get(i).values().stream().map(String::valueOf).collect(Collectors.joining(";"));
            titles.add(collect);
        }
        LambdaQueryWrapper<TitleIdentify> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TitleIdentify::getTitleRow, titles).orderByDesc(TitleIdentify::getCreatedTime);
        List<TitleIdentify> list = titleIdentifyService.list(queryWrapper);
        if (CollUtil.isNotEmpty(list)) {
            result.put("number", Integer.valueOf(list.get(0).getRowNumber() == null ? "0" : list.get(0).getRowNumber()));
            result.put("titleRowSourceId", list.get(0).getId());
            result.put("titleIdentify", list.get(0));
            return result;
        } else {
            // 遍历每一行，检查该行是否符合标题行的条件
//            for (int i = 0; i < rows.size(); i++) {
//                boolean isTitleRow = rows.get(i).stream()
//                        .allMatch(data -> StrUtil.isNotBlank(data.getV())); // 检查该行的每一列是否都不为空
//                if (isTitleRow) {
//                    result.put("number", i);
//                    return result; // 找到标题行，返回其索引
//                }
//            }
            result.put("number", 0);
        }
        return result;

    }

    /**
     * 获取标题行与数据库字段的对应关系
     *
     * @param titleData Excel中的标题行数据，dataTableId所属数据库的ID
     * @return 标题行与数据库字段的对应关系
     */
    public List<ExcelBasicImportVo> getTitleConfig(Map<Integer, String> titleData, String dataTableId) {
        List<ExcelBasicImportVo> finalTitleList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(dataTableId)){
            QueryWrapper<TitleCorrespondence> titleCorrespondenceQueryWrapper = new QueryWrapper<>();
            titleCorrespondenceQueryWrapper.lambda().eq(TitleCorrespondence::getTitleMenuId, dataTableId);
            Map<String, TitleCorrespondence> column = titleCorrespondenceService.list(titleCorrespondenceQueryWrapper).stream()
                    .collect(Collectors.toMap(TitleCorrespondence::getExcelColumnName, Function.identity()));
            // 给表头设置对应关系
            titleData.entrySet()
                    .forEach(entry -> {
                        ExcelBasicImportVo importVo = new ExcelBasicImportVo();
                        importVo.setExcelColumnName(entry.getValue());
                        importVo.setSetFieldDesc(column.get(entry.getValue()) == null ? "" : column.get(entry.getValue()).getSetFieldDesc());
                        importVo.setSetFieldName(column.get(entry.getValue()) == null ? "" : column.get(entry.getValue()).getNameField());
                        importVo.setExcelColumnIndex(String.valueOf(entry.getKey()));
                        importVo.setTitleFieldId(column.get(entry.getValue()) == null ? "" : column.get(entry.getValue()).getId());
                        finalTitleList.add(importVo);
                    });
        }else {
            // 给表头赋值
            titleData.entrySet()
                    .forEach(entry -> {
                        ExcelBasicImportVo importVo = new ExcelBasicImportVo();
                        importVo.setExcelColumnName(entry.getValue());
                        importVo.setSetFieldDesc("");
                        importVo.setSetFieldName("");
                        importVo.setExcelColumnIndex(String.valueOf(entry.getKey()));
                        importVo.setTitleFieldId("");
                        finalTitleList.add(importVo);
                    });
        }
        return finalTitleList;
    }



}
