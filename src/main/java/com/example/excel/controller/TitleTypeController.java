package com.example.excel.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import com.example.excel.common.Result;
import com.example.excel.common.WebConstant;
import com.example.excel.custom.CustomBaseController;
import com.example.excel.custom.CustomPage;
import com.example.excel.entity.TitleCorrespondence;
import com.example.excel.entity.TitleFields;
import com.example.excel.entity.TitleMenu;
import com.example.excel.entity.TitleType;
import com.example.excel.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class TitleTypeController extends CustomBaseController<TitleTypeService, TitleType> {
    private final String PATH = WebConstant.API_PATH + "/title_type";


    private final TitleMenuService titleMenuService;
    private final FileImportLogService fileImportLogService;
    private final TitleFieldsService titleFieldsService;
    private final TitleCorrespondenceService titleCorrespondenceService;

    public TitleTypeController(TitleMenuService titleMenuService, FileImportLogService fileImportLogService, TitleFieldsService titleFieldsService, TitleCorrespondenceService titleCorrespondenceService) {
        this.titleMenuService = titleMenuService;
        this.fileImportLogService = fileImportLogService;
        this.titleFieldsService = titleFieldsService;
        this.titleCorrespondenceService = titleCorrespondenceService;
    }


    @SneakyThrows
    @Operation(summary = "分页")
    @PostMapping(value = PATH + "/page")
    public Result<?> page(@RequestBody TitleType form, CustomPage<TitleType> page, @RequestParam String menuCode) {
        QueryWrapper<TitleType> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .orderByDesc(TitleType::getCreatedTime);
        IPage<TitleType> result = this.service.page(page, queryWrapper);
        Map<String, String> menuMap = titleMenuService.list().stream().collect(Collectors.toMap(TitleMenu::getId, TitleMenu::getName));
        Map<String, List<TitleFields>> typeMap = titleFieldsService.list().stream().filter(s->!s.getNameField().equals("className")).collect(Collectors.groupingBy(title -> title.getType() == null ? "null" : title.getType() ));
        result.getRecords().forEach(d->{
            d.setSecondFormId(menuMap.get(d.getTitleMenuId()));
            if (StrUtil.isNotBlank(d.getFieldIds())){
                //手动添加字段
                d.setFieldIds(typeMap.get(d.getId()).stream().map(TitleFields::getName).collect(Collectors.joining(",")));
            }
            if (d.getName().equals("默认字段")){
                // 默认字段
               d.setFieldIds(typeMap.get("null").stream().filter(s->s.getTitleMenuId().equals(d.getTitleMenuId())).map(TitleFields::getName).collect(Collectors.joining(",")));
            }
        });
        return Result.OK(result);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping(value = PATH + "/select")
    public Result<?> select(String id) {
        return Result.OK(this.service.getById(id));
    }

    @Operation(summary = "添加")
    @PostMapping(value = PATH + "/add")
    public Result<?> add(@RequestBody TitleType form) {
        long id = IdWorker.getId();
        form.setId(String.valueOf(id));
        List<String> ids = form.getDataList().stream()
                .map(TitleFields::getId) // 提取 id
                .collect(Collectors.toList());
        form.setFieldIds(String.join(",", ids));
        List<TitleFields> titleFields = titleFieldsService.list(new QueryWrapper<TitleFields>().in("id", ids));
        List<TitleFields> collect = titleFields.stream().map(v -> v.setType(form.getId())).collect(Collectors.toList());
        List<TitleCorrespondence> titleCorrespondences = titleCorrespondenceService.list(new QueryWrapper<TitleCorrespondence>().eq("type", form.getId()));
        List<TitleCorrespondence> collect1 = titleCorrespondences.stream().map(d -> d.setType(form.getId())).collect(Collectors.toList());
        titleFieldsService.updateBatchById(collect);
        titleCorrespondenceService.updateBatchById(collect1);

        return Result.OK(this.service.save(form));
    }


    @Operation(summary = "编辑")
    @PostMapping(value = PATH + "/edit")
    public Result<?> edit(@RequestBody TitleType form) {
        List<String> ids = form.getDataList().stream()
                .map(TitleFields::getId) // 提取 id
                .collect(Collectors.toList());
        form.setFieldIds(String.join(",", ids));
        //找到删除的哪些数据把他删掉
        List<TitleFields> oldDataList = titleFieldsService.list(new QueryWrapper<TitleFields>().eq("type", form.getId()));
        List<String> finalOldIds = oldDataList.stream().map(TitleFields::getId).collect(Collectors.toList());
        List<TitleCorrespondence> titleCorrespondences = titleCorrespondenceService.list(new QueryWrapper<TitleCorrespondence>().eq("type", form.getId()));
        List<String> titleCorrespondencesFinalOldIds  = titleCorrespondences.stream().map(TitleCorrespondence::getId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(finalOldIds)){
            UpdateWrapper<TitleFields> queryWrapper=new UpdateWrapper<>();
            queryWrapper.lambda().in(TitleFields::getId, finalOldIds)
                            .set(TitleFields::getType, null);
            titleFieldsService.update(queryWrapper);
        }
        if (CollUtil.isNotEmpty(titleCorrespondencesFinalOldIds)){
            UpdateWrapper<TitleCorrespondence> finalTitleCorrespondences = new UpdateWrapper<>();
            finalTitleCorrespondences.lambda().in(TitleCorrespondence::getId, titleCorrespondencesFinalOldIds)
                    .set(TitleCorrespondence::getType, null);
            titleCorrespondenceService.update(finalTitleCorrespondences);
        }

        //更新集合
        if (CollUtil.isNotEmpty(ids)) {
            List<TitleFields> titleFields = titleFieldsService.list(new QueryWrapper<TitleFields>().in("id", ids));
            List<TitleCorrespondence> titleCorrSaveList = titleCorrespondenceService.list(new QueryWrapper<TitleCorrespondence>().lambda().in(TitleCorrespondence::getTitleFieldId, ids));

            List<TitleFields> collect = titleFields.stream().map(v -> v.setType(form.getId())).collect(Collectors.toList());
            List<TitleCorrespondence> saveList = titleCorrSaveList.stream().map(d -> d.setType(form.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(collect)){
                titleFieldsService.updateBatchById(collect);
            }
            if (CollUtil.isNotEmpty(saveList)){
                titleCorrespondenceService.updateBatchById(saveList);
            }
        }


        return Result.OK(this.service.updateById(form));
    }

    @Operation(summary = "删除")
    @PostMapping(value = PATH + "/remove")
    public Result<?> remove(String id) {
        TitleType byId = this.service.getById(id);
        if (byId.getName().contains("默认字段")){
            return Result.error("默认字段不能删除");
        }
        return Result.OK(this.service.removeById(id));
    }

//    @Operation(summary = "批量删除")不能批量删除
//    @OperateLog(describe = "批量删除信息", actionType = ActionType.DELETE_ACTION)
//    @PostMapping(value = PATH + "/remove_batch")
//    public Result<?> removeBatch(@RequestBody List<String> ids) {
//        return Result.OK(this.service.removeBatchByIds(ids));
//    }
    @Operation(summary = "list查询")
    @PostMapping(value = PATH + "/list")
    public Result<?> list(@RequestBody TitleType form) {
        if (StrUtil.isBlank(form.getTwoFormId())){
            return Result.error("请选择表类型");
        }
        QueryWrapper<TitleType> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StrUtil.isNotBlank(form.getTwoFormId()), TitleType::getTitleMenuId, form.getTwoFormId());
        Map<String, List<TitleFields>> typeMap = titleFieldsService.list(new LambdaQueryWrapper<TitleFields>().eq(TitleFields::getTitleMenuId, form.getTwoFormId()))
                .stream().filter(s->!s.getNameField().equals("className")).collect(Collectors.groupingBy(title -> title.getType() == null ? "null" : title.getType() ));
        List<TitleType> collect = this.service.list(queryWrapper).stream().peek(d -> {
            if (StrUtil.isNotBlank(d.getName()) && d.getName().equals("默认字段")) {
                d.setFieldIds(typeMap.get("null").stream().filter(s->s.getTitleMenuId().equals(d.getTitleMenuId())).map(TitleFields::getId).collect(Collectors.joining(",")));
            }
        }).collect(Collectors.toList());
        return Result.OK(collect);
    }

}
