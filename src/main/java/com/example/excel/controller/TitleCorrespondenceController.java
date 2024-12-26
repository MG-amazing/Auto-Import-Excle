package com.example.excel.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.example.excel.common.Result;
import com.example.excel.common.WebConstant;
import com.example.excel.custom.CustomBaseController;
import com.example.excel.custom.CustomPage;
import com.example.excel.entity.TitleCorrespondence;
import com.example.excel.entity.TitleFields;
import com.example.excel.entity.TitleMenu;
import com.example.excel.entity.TitleType;
import com.example.excel.service.TitleCorrespondenceService;
import com.example.excel.service.TitleFieldsService;
import com.example.excel.service.TitleMenuService;
import com.example.excel.service.TitleTypeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TitleCorrespondenceController extends CustomBaseController<TitleCorrespondenceService, TitleCorrespondence> {
    private final String PATH = WebConstant.API_PATH + "/title_correspondence";



    private final TitleFieldsService titleFieldsService;
    private final TitleMenuService titleMenuService;
    private final TitleTypeService titleTypeService;

    public TitleCorrespondenceController(TitleFieldsService titleFieldsService, TitleMenuService titleMenuService, TitleTypeService titleTypeService) {
        this.titleFieldsService = titleFieldsService;
        this.titleMenuService = titleMenuService;
        this.titleTypeService = titleTypeService;
    }

    @SneakyThrows
    @Operation(summary = "分页")
    @PostMapping(value = PATH + "/page")
    public Result<?> page(@RequestBody TitleCorrespondence form, CustomPage<TitleCorrespondence> page, @RequestParam String menuCode) {
        boolean flag= false;
        if (StrUtil.isNotBlank(form.getSecondFormId())) {
            TitleType byId = titleTypeService.getById(form.getSecondFormId());
            flag = StrUtil.isNotBlank(byId.getName())&&!byId.getName().equals("默认字段");
        }
        QueryWrapper<TitleCorrespondence> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StrUtil.isNotBlank(form.getExcelColumnName()), TitleCorrespondence::getExcelColumnName, form.getExcelColumnName())
                .eq(StrUtil.isNotBlank(form.getThreeFormId()), TitleCorrespondence::getTitleFieldId, form.getThreeFormId())
                .eq(StrUtil.isNotBlank(form.getSecondFormId())&&flag, TitleCorrespondence::getType, form.getSecondFormId())
                .orderByDesc(TitleCorrespondence::getCreatedTime);
        if (StrUtil.isNotBlank(form.getSecondFormId())){
            queryWrapper.lambda().isNull(!flag,TitleCorrespondence::getType);;
        }

        IPage<TitleCorrespondence> result = this.service.page(page, queryWrapper);

        return Result.OK(result);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping(value = PATH + "/select")
    public Result<?> select(String id) {
        return Result.OK(this.service.getById(id));
    }

    @Operation(summary = "添加")
    @PostMapping(value = PATH + "/add")
    public Result<?> add(@RequestBody TitleCorrespondence form) {
        String titleFieldId = form.getTitleFieldId();

        if (StrUtil.isBlank(titleFieldId)){
            return Result.error("titleFieldId为空");
        }
        TitleFields byId = titleFieldsService.getById(titleFieldId);
        TitleMenu byId1 = titleMenuService.getById(byId.getTitleMenuId());
        form.setType(byId.getType());
        form.setTitleMenuName(byId.getMenuName());
        form.setTitleMenuId(byId.getTitleMenuId());
        form.setNameField(byId.getNameField());
        form.setNameEntity(byId1.getEntity());

        return Result.OK(this.service.save(form));
    }


    @Operation(summary = "编辑")
    @PostMapping(value = PATH + "/edit")
    public Result<?> edit(@RequestBody TitleCorrespondence form) {
        return Result.OK(this.service.updateById(form));
    }

    @Operation(summary = "删除")
    @PostMapping(value = PATH + "/remove")
    public Result<?> remove(String id) {
        return Result.OK(this.service.removeById(id));
    }

    @Operation(summary = "批量删除")
    @PostMapping(value = PATH + "/remove_batch")
    public Result<?> removeBatch(@RequestBody List<String> ids) {
        return Result.OK(this.service.removeByIds(ids));
    }
    @Operation(summary = "list查询")
    @PostMapping(value = PATH + "/list")
    public Result<?> list(@RequestBody TitleCorrespondence form) {
        return Result.OK(this.service.list());
    }

}
