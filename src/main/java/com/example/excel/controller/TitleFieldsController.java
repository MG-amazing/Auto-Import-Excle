package com.example.excel.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.excel.common.Result;
import com.example.excel.common.WebConstant;
import com.example.excel.custom.CustomBaseController;
import com.example.excel.custom.CustomPage;
import com.example.excel.entity.TitleFields;
import com.example.excel.service.TitleFieldsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

@RestController
public class TitleFieldsController extends CustomBaseController<TitleFieldsService, TitleFields> {
    private final String PATH = WebConstant.API_PATH + "/TitleField";

    public TitleFieldsController(TitleFieldsService service) {
        super(service);
    }

    @SneakyThrows
    @Operation(summary = "分页")
    @PostMapping(value = PATH + "/page")
    public Result<?> page(@RequestBody TitleFields form, CustomPage<TitleFields> page, @RequestParam String menuCode) {
        QueryWrapper<TitleFields> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(StrUtil.isNotBlank(form.getName()), TitleFields::getName, form.getName())
                .eq(StrUtil.isNotBlank(form.getTitleMenuId()), TitleFields::getTitleMenuId, form.getTitleMenuId())
                .eq(StrUtil.isNotBlank(form.getIsDeduplicate()), TitleFields::getIsDeduplicate, form.getIsDeduplicate())
                .eq(StrUtil.isNotBlank(form.getIsExtract()), TitleFields::getIsExtract, form.getIsExtract())
                .ne(TitleFields::getNameField, "className")
                .orderByDesc(TitleFields::getCreatedTime);
        IPage<TitleFields> result = this.service.page(page, queryWrapper);
        return Result.OK(result);
    }


    @Operation(summary = "根据ID查询")
    @GetMapping(value = PATH + "/select")
    public Result<?> select(String id) {
        return Result.OK(this.service.getById(id));
    }

    @Operation(summary = "编辑")
    @PostMapping(value = PATH + "/edit")
    public Result<?> edit(@RequestBody TitleFields form) {
        UpdateWrapper<TitleFields>updateWrapper=new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(TitleFields::getId,form.getId())
                .set(StrUtil.isNotBlank(form.getIsDeduplicate()),TitleFields::getIsDeduplicate,form.getIsDeduplicate())
                .set(StrUtil.isNotBlank(form.getIsExtract()),TitleFields::getIsExtract,form.getIsExtract());
        return Result.OK(this.service.update(updateWrapper));
    }


}
