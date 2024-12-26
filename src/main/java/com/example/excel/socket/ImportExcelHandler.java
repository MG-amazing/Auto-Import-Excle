package com.example.excel.socket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.example.excel.dto.ExcelBasicImportVo;
import com.example.excel.dto.ResultMessage;
import com.example.excel.dto.TitleImportDataDto;
import com.example.excel.entity.*;
import com.example.excel.service.*;
import com.example.excel.util.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class ImportExcelHandler extends TextWebSocketHandler {

    public String DATA_TABLE_ENTITY = "DATA::LOCK::";
    private final ObjJsonUtil objJsonUtil;
    private final FileImportLogServiceUtil fileImportLogServiceUtil;
    private final GetBeanUtil getBeanUtil;
    private final RedisServiceUtil redisServiceUtil;
    private final TitleCorrespondenceService titleCorrespondenceService;
    private final TitleTypeService titleTypeService;
    private final TitleMenuService titleMenuService;
    private final TitleIdentifyService titleIdentifyService;
    private final TitleMajorService titleMajorService;
    private final TitleFieldsService titleFieldsService;

    public ImportExcelHandler(ObjJsonUtil objJsonUtil, FileImportLogServiceUtil fileImportLogServiceUtil, GetBeanUtil getBeanUtil, RedisServiceUtil
            redisServiceUtil, TitleCorrespondenceService titleCorrespondenceService, TitleTypeService titleTypeService,
                              TitleMenuService titleMenuService, TitleIdentifyService titleIdentifyService, TitleMajorService titleMajorService, TitleFieldsService titleFieldsService) {
        this.objJsonUtil = objJsonUtil;
        this.fileImportLogServiceUtil = fileImportLogServiceUtil;
        this.getBeanUtil = getBeanUtil;
        this.redisServiceUtil = redisServiceUtil;
        this.titleCorrespondenceService = titleCorrespondenceService;
        this.titleTypeService = titleTypeService;
        this.titleMenuService = titleMenuService;
        this.titleIdentifyService = titleIdentifyService;
        this.titleMajorService = titleMajorService;
        this.titleFieldsService = titleFieldsService;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 客户端连接建立后执行的操作

        System.out.println("Client connected: " + session.getId());
        session.sendMessage(new TextMessage(getMessageJson("连接成功")));

    }
    public String getMessageJson(String resultMessage ){
      return JSONObject.toJSONString(new ResultMessage(resultMessage));
    }
    public String getPercentJson(Double json){
       return JSONObject.toJSONString(new ResultMessage(json));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发送的消息
        String payload = message.getPayload();

        // 将接收到的 JSON 字符串转换为 对象
        List<TitleImportDataDto> titleImportDataDtos = objJsonUtil.parseJson(payload, TitleImportDataDto.class);
        if (CollUtil.isEmpty(titleImportDataDtos)) {
            session.sendMessage(new TextMessage(getMessageJson("没有匹配的标题")));
        } else {
            processFiles(titleImportDataDtos, session);
        }

        // 回传消息给客户端（初始消息）
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 连接关闭后执行的操作
        System.out.println("Client disconnected: " + session.getId());
    }

    // 封装文件处理和进度更新的逻辑
    public void processFiles(List<TitleImportDataDto> titleImportDataDtos, WebSocketSession session) throws Exception {
        // 线程池来管理并发任务
        ExecutorService executorService = Executors.newFixedThreadPool(5); // 线程池大小可以根据需求调整
        String thread = session.getId();

        // 使用 AtomicInteger 来跟踪已处理的任务数
        AtomicInteger processedCount = new AtomicInteger(0);
        int totalTasks = titleImportDataDtos.size(); // 总任务数
        ExecutorService messageThread = Executors.newFixedThreadPool(1); // 线程池大小可以根据需求调整

        messageThread.execute(() -> {
            while (processedCount.get() < totalTasks) {
                try {
                    Thread.sleep(200); // 每秒检查一次进度
                    int progress = (int) ((double) processedCount.get() / totalTasks * 100);
                    // 只有在进度变化时才发送消息
                    sendMessageWithLock(session, thread, getPercentJson((double) progress));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        sendMessageWithLock(session, thread, getPercentJson((double) 0));
        //设置批次号
        String batchNumber = UUID.randomUUID().toString();

        // 提交任务到线程池
        titleImportDataDtos.forEach(d -> executorService.submit(() -> {
            try {
                // 处理每个文件
                sendMessageWithLock(session, thread, getMessageJson("开始解析：" + d.getSheetName() + "---" + d.getFilePath()));
                List<ExcelBasicImportVo> titleList = d.getTitleList();
                Integer titleRow = d.getTitleRow();
                List<Map<Integer, String>> maps = fileImportLogServiceUtil.readFromFile(d.getFilePath());

                List<Map<Integer, String>> data = maps.stream().skip(titleRow + 1).collect(Collectors.toList());//忽略标题行及之前的行
                //转对象
                List<Object> list = convertToObjects(data, titleList, d.getDataTableEntity(), batchNumber);

                sendMessageWithLock(session, thread, getMessageJson("开始导入：" + d.getSheetName()));
                boolean b = saveData(d.getDataTableEntity(), list, d.getTitleRow(), d.getTitleRowSourceId(), d.getTitleList(), d.getDataTableId(), d.getDataPartId(), d.getTenantId());
                Thread.sleep(1000);

                // 每个任务处理完，更新进度
                int completedTasks = processedCount.incrementAndGet();
                int progress = (int) ((double) completedTasks / totalTasks * 100);

                // 更新进度并将进度发送给客户端
                if (progress == 100) {
                    session.sendMessage(new TextMessage(getMessageJson("已完成")));
                    session.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendMessageWithLock(session, thread, getMessageJson("导入：" + d.getSheetName() + "失败:  " + e.getMessage()));
                } catch (Exception ex) {
                    e.printStackTrace();
                }
            }
        }));

        // 关闭线程池
        executorService.shutdown();
        messageThread.shutdown();
    }

    @Transactional(rollbackFor = Exception.class)

    public boolean saveData(String dataTableEntity, List<Object> list, Integer rowNum, String titleRowSourceId, List<ExcelBasicImportVo> titleRow, String dataTableId, String dataPartId, String tenantId) throws Exception {
        boolean b = redisServiceUtil.setRedisLock(DATA_TABLE_ENTITY + dataTableEntity, dataTableEntity, 180);
        if (!b) {
            Thread.sleep(500);//拿不到就睡觉
            return saveData(dataTableEntity, list, rowNum, titleRowSourceId, titleRow, dataTableId, dataPartId, tenantId);
        }
        //把每次传过来的titleRow解析
        Map<String, TitleCorrespondence> oldMap = titleCorrespondenceService
                .list(new LambdaQueryWrapper<TitleCorrespondence>().eq(TitleCorrespondence::getTitleMenuId, dataTableId))
                .stream()
                .collect(Collectors.toMap(
                        correspondence -> correspondence.getExcelColumnName() + correspondence.getNameField(), // 键是拼接的字符串
                        Function.identity() // 值是对应的 TitleCorrespondence 对象
                ));
        Map<String, ExcelBasicImportVo> newMap = titleRow.stream().collect(Collectors.toMap(s -> s.getExcelColumnName() + s.getSetFieldName(), Function.identity()));
        Map<String, String> typeMap = titleTypeService
                .list(new LambdaQueryWrapper<TitleType>().eq(TitleType::getTitleMenuId, dataTableId))
                .stream()
                .filter(titleType -> StrUtil.isNotBlank(titleType.getFieldIds()))
                .flatMap(titleType -> {
                    // 获取 getFieldIds，拆分为数组，并创建每个 fieldId 与 getId() 的映射
                    return Arrays.stream(titleType.getFieldIds().split(","))
                            .map(fieldId -> new AbstractMap.SimpleEntry<>(fieldId, titleType.getId()));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<TitleCorrespondence> titleCorrespondenceList = new ArrayList<>();
        TitleMenu byId = titleMenuService.getById(dataTableId);
        newMap.forEach((key, value) -> {
            if (!oldMap.containsKey(key)) {
                TitleCorrespondence titleCorrespondence = new TitleCorrespondence();
                titleCorrespondence.setSetFieldDesc(value.getSetFieldDesc());
                titleCorrespondence.setExcelColumnName(value.getExcelColumnName());
                titleCorrespondence.setTitleMenuId(dataTableId);
                titleCorrespondence.setNameField(value.getSetFieldName());
                titleCorrespondence.setNameEntity(dataTableEntity);
                titleCorrespondence.setType(typeMap.get(value.getTitleFieldId()));
                titleCorrespondence.setTitleMenuName(byId.getName());
                titleCorrespondence.setTitleFieldId(value.getTitleFieldId());
                titleCorrespondenceList.add(titleCorrespondence);
            }
        });
        TitleMajor byId1 = titleMajorService.getById(dataPartId);
        String names = titleRow.stream().map(ExcelBasicImportVo::getExcelColumnName).collect(Collectors.joining(";"));
        String isDeduplicate = titleFieldsService.list(new LambdaQueryWrapper<TitleFields>().eq(TitleFields::getTitleMenuId, dataTableId)).stream()
                .filter(s -> !s.getIsDeduplicate().equals("0"))
                .map(TitleFields::getNameField)
                .collect(Collectors.joining(","));


        if (b) {
            try {
                // 事务内执行批量保存操作
                Map<String, Object> resultMap = getBeanUtil.invokeSaveOrUpdateBatch(dataTableEntity, list, isDeduplicate, tenantId);
                if (StrUtil.isNotBlank(titleRowSourceId)) {
                    UpdateWrapper<TitleIdentify> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.lambda().set(TitleIdentify::getRowNumber, rowNum)
                            .eq(TitleIdentify::getId, titleRowSourceId)
                            .set(TitleIdentify::getType, "导入数据时自动生成");

                    titleIdentifyService.update(updateWrapper);
                } else {
                    TitleIdentify titleIdentify = new TitleIdentify();
                    titleIdentify.setTitleRow(names);
                    titleIdentify.setType("导入数据时自动生成");
                    titleIdentify.setEntity(dataTableEntity);
                    titleIdentify.setRowNumber(String.valueOf(rowNum));
                    titleIdentify.setOneFormId(dataPartId);
                    titleIdentify.setOneFormName(byId1.getName());
                    titleIdentify.setTwoFormId(dataTableId);
                    titleIdentify.setTwoFormName(byId.getName());

                    titleIdentifyService.save(titleIdentify);
                }

                titleCorrespondenceService.saveBatch(titleCorrespondenceList);

                // 执行其他保存逻辑...

                // 如果保存成功，退出循环
            } catch (Exception e) {
                e.printStackTrace();
                // 捕获并打印异常
                throw new RuntimeException(e.getMessage());

                // 可以在此处增加重试逻辑，或者在事务失败后做一些其他处理
                // 比如：记录日志、通知管理员等
            } finally {
                // 确保在finally中释放锁
                redisServiceUtil.deleteKey(DATA_TABLE_ENTITY + dataTableEntity);
            }
        }
        return false;

    }

    public static List<Object> convertToObjects(List<Map<Integer, String>> dataList, List<ExcelBasicImportVo> titleList, String dataTableEntity, String batchNumber) {
        List<Object> resultList = new ArrayList<>();

        // 遍历每一行数据
        for (Map<Integer, String> row : dataList) {
            // 使用 ClassObjUtil 创建对象实例
            Object obj = null;
            try {
                obj = ClassObjUtil.createObject(dataTableEntity);
                //设置批次号
                ReflectionUtil.setFieldValue(obj, "batchId", batchNumber);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 遍历表头信息，将值设置到对应字段
            for (ExcelBasicImportVo title : titleList) {
                int columnIndex = Integer.parseInt(title.getExcelColumnIndex());
                String fieldName = title.getSetFieldName();
                String value = row.get(columnIndex); // 获取列对应的值

                // 使用 ReflectionUtil 设置字段值
                try {
                    if (StrUtil.isNotBlank(fieldName)) {
                        ReflectionUtil.setFieldValue(obj, fieldName, value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            // 添加到结果列表
            resultList.add(obj);
        }

        return resultList;
    }

    public void sendMessageWithLock(WebSocketSession session, String thread,String message) throws Exception {
        // 获取分布式锁
        boolean lockAcquired = redisServiceUtil.setRedisLock(DATA_TABLE_ENTITY + "Message::Lock::" + thread, "lock", 1);

        // 如果成功获取锁，发送消息
        if (lockAcquired) {
            try {
                sendMessage(session, message);
            } catch (Exception e) {
                // 处理发送消息时的异常
                e.printStackTrace();
            } finally {
                // 释放锁
                redisServiceUtil.deleteKey(DATA_TABLE_ENTITY + "Message::Lock::" + thread);
            }
        } else {
            // 如果无法获取锁，可以选择等待一段时间或直接返回失败
            Thread.sleep(50);
            sendMessageWithLock(session, thread, message);
        }
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            // 发送消息到指定WebSocket会话
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (Exception e) {
            // 处理WebSocket消息发送失败的情况
            e.printStackTrace();
        }
    }


}
