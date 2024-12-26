package com.example.excel.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelImportUtils extends AnalysisEventListener<Map<Integer, String>> {

    @Getter
    private final List<Sheet> sheetDataList = new ArrayList<>(); // 存储每个 Sheet 页的数据

    private List<Data> currentRowData = new ArrayList<>(); // 当前行数据
    private List<Map<Integer, String>> currentSheetRows = new ArrayList<>(); // 当前 sheet 页的所有行

    private String currentSheetName = "Sheet1"; // 默认 Sheet 名

    private Map<Integer, String> headerMap = new LinkedHashMap<>(); // 存储表头信息

    private boolean keepEmptyCells; // 是否保留空单元格

    /**
     * 构造函数，接受一个布尔值，决定是否保留空值。
     */
    public ExcelImportUtils(boolean keepEmptyCells) {
        this.keepEmptyCells = keepEmptyCells;
    }

    /**
     * 数据对象，包含值和位置。
     */
    @Getter
    @Setter
    public static class Data implements Serializable {
        private String v;
        private int[] p; // [rowIndex, columnIndex]

        public Data(String value, int rowIndex, int columnIndex) {
            this.v = value;
            this.p = new int[]{rowIndex, columnIndex};
        }
    }

    @Getter
    @Setter
    public static class Sheet implements Serializable {
        private String sheetName;
        private List<Map<Integer, String>> rows; // 每一行的数据

        public Sheet(String sheetName, List<Map<Integer, String>> rows) {
            this.sheetName = sheetName;
            this.rows = rows;
        }
    }

    /**
     * 静态方法：读取 Excel 文件并返回结果。
     * 传 true 空单元格也显示不传就不显示
     */
    public static List<Sheet> readExcel(MultipartFile file, boolean keepEmptyCells) {
        List<Sheet> result = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream()) {
            ExcelImportUtils listener = new ExcelImportUtils(keepEmptyCells);
            EasyExcel.read(inputStream, listener).headRowNumber(-1).doReadAll();
            result = listener.getSheetDataList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 读取每一行数据。
     */
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex(); // 获取行号

//        // 清空当前行数据
//        currentRowData.clear();
//
//        // 遍历该行的所有列数据
//        data.forEach((columnIndex, value) -> {
//            if (value != null || keepEmptyCells) {
//                // 如果值为空且需要保留空单元格，则添加空值
//                currentRowData.add(new Data(value != null ? value : "", rowIndex, columnIndex));
//            }
//        });
        Map<Integer, String> currentRowData = new LinkedHashMap<>();
        data.forEach((columnIndex, value) -> {
                currentRowData.put(columnIndex, value != null ? value : "");
        });
        currentSheetRows.add(currentRowData);

//        // 将当前行数据添加到当前 sheet 页的行列表
//        if (!currentRowData.isEmpty()) {
//            currentSheetRows.add(data);
//        }
    }

    /**
     * 每个 Sheet 页开始时的处理。
     * 模拟 onSheetStart 方法的功能
     */
    public String onSheetStart(AnalysisContext context) {
        // 获取当前 Sheet 的名称
        return context.readSheetHolder().getSheetName();
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 确保当前 sheet 页读取的数据已经被添加到 sheetDataList
        if (!currentSheetRows.isEmpty()) {
            sheetDataList.add(new Sheet(onSheetStart(context), new ArrayList<>(currentSheetRows)));
            currentSheetRows.clear();
        }

        // 打印读取的数据（调试用）
        System.out.println("Sheet Data: " + sheetDataList);
    }

    /**
     * 处理表头数据。
     */
    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        headMap.forEach((columnIndex, cellData) -> {
            if (cellData != null && cellData.getStringValue() != null) {
                headerMap.put(columnIndex, cellData.getStringValue());
            }
        });
    }

    /**
     * Excel 读取失败的处理。
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) {
        exception.printStackTrace();
    }
}
