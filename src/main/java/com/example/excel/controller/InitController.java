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
import com.example.excel.custom.CustomPage;
import com.example.excel.dto.FieldUpdateDto;
import com.example.excel.entity.TitleFields;
import com.example.excel.entity.TitleMenu;
import com.example.excel.entity.TitleType;
import com.example.excel.service.*;
import com.example.excel.util.FiledUtil;
import com.example.excel.util.GetBeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j

public class InitController {
    private final String PATH = WebConstant.API_PATH + "/title";
    @Value("${isinit.init}")
    private boolean isInitData;
    @Value("${isinit.isforce}")
    private boolean isInitDataForce;

    @PostMapping(PATH + "/initData")
    public Result<?> init() {
        if (isInitData) {
            log.info("执行初始化库操作");
            try {
                initData();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("未知异常终止启动");
            }

        } else {
            log.info("不执行初始化库操作");
        }
        return Result.OK();
    }


    public InitController(TitleMenuService titleMenuService, TitleMajorService titleMajorService, TitleCorrespondenceService titleCorrespondenceService, TitleTypeService titleTypeService, TitleFieldsService titleFieldsService, GetBeanUtil getBeanUtil) {
        this.titleMenuService = titleMenuService;
        this.titleMajorService = titleMajorService;
        this.titleCorrespondenceService = titleCorrespondenceService;
        this.titleTypeService = titleTypeService;
        this.titleFieldsService = titleFieldsService;
        this.getBeanUtil = getBeanUtil;
    }

    //注入各个业务层
    private final TitleMenuService titleMenuService;
    private final TitleMajorService titleMajorService;
    private final TitleCorrespondenceService titleCorrespondenceService;
    private final TitleTypeService titleTypeService;
    private final TitleFieldsService titleFieldsService;
    private final GetBeanUtil getBeanUtil;



    @Transactional(rollbackFor = Exception.class)
    public void initData() throws Exception {
        Map<String, Map<String, String>> stringMapMap = FiledUtil.extractFromEntities(Object.class);
        List<TitleMenu> titleMenus = titleMenuService.list();
        List<TitleFields> titleFields = titleFieldsService.list();
        //判断数据库是否为空

        // 遍历数据库中的字段，检查是否存在于新传入的解析结果中
        List<String> titleFieldsDeleteList = titleFields.stream()
                .filter(field -> !stringMapMap.containsKey(field.getMenuName()) ||
                        !stringMapMap.get(field.getMenuName()).containsKey(field.getNameField())).map(TitleFields::getId).collect(Collectors.toList());
        List<TitleType> typeList = titleTypeService.list();
        //判断数据是否存在不存在插入
        stringMapMap.forEach((key, value) -> {
            boolean menuExists = titleMenus.stream()
                    .anyMatch(menu -> menu.getName().equals(key)); // 判断是否已存在

            long id = IdWorker.getId();


            if (!menuExists) {
                TitleMenu titleMenu = new TitleMenu();
                titleMenu.setName(key);
                titleMenu.setTitleMajorId("1");
                titleMenu.setEntity(value.get("className"));
                titleMenu.setId(String.valueOf(id));
                titleMenus.add(titleMenu); // 添加到集合

                TitleType type = new TitleType();
                type.setName("默认字段");
                type.setTitleMenuId(String.valueOf(id));
                typeList.add(type);
            }


            value.forEach((key1, value1) -> {
                boolean fieldExists = titleFields.stream()
                        .anyMatch(field -> field.getNameField().equals(key1) && field.getMenuName().equals(key));
                if (!fieldExists) {
                    TitleFields field = new TitleFields();
                    field.setNameField(key1);
                    field.setTitleMenuId(String.valueOf(id));
                    field.setName(value1);
                    field.setIsDeduplicate("0");
                    field.setIsExtract("0");
                    field.setDeDefault("0");
                    field.setExDefault("0");
                    field.setMenuName(key);
                    titleFields.add(field); // 添加到集合
                }

            });
        });


        this.titleMenuService.saveOrUpdateBatch(titleMenus);
        this.titleFieldsService.saveOrUpdateBatch(titleFields);
        titleTypeService.saveOrUpdateBatch(typeList);

        if (CollUtil.isNotEmpty(titleFieldsDeleteList)) {
            titleFieldsService.removeByIds(titleFieldsDeleteList);
        }


    }

    @PostMapping(PATH + "/level_one_list")
    public Result<?> levelOne() {
        return Result.OK(titleMajorService.list());
    }

    @PostMapping(PATH + "/level_second_list")
    public Result<?> levelSecond(@RequestBody TitleMenu form) {
        LambdaQueryWrapper<TitleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(form.getOneFormId()), TitleMenu::getTitleMajorId, form.getOneFormId());
        return Result.OK(titleMenuService.list(queryWrapper));
    }

    @PostMapping(PATH + "/level_third_list")
    public Result<?> levelThird(@RequestBody TitleFields form) {
        boolean flag=false;
        if (StrUtil.isNotBlank(form.getSecondFormId())){
            TitleType byId = titleTypeService.getById(form.getSecondFormId());
            flag = StrUtil.isNotBlank(byId.getName())&&!byId.getName().equals("默认字段");
        }
        LambdaQueryWrapper<TitleFields> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(StrUtil.isNotBlank(form.getTwoFormId()), TitleFields::getTitleMenuId, form.getTwoFormId())
                .eq(StrUtil.isNotBlank(form.getSecondFormId())&&flag, TitleFields::getType, form.getSecondFormId())
                .ne(!StrUtil.isNotBlank(form.getIsGetEntity()), TitleFields::getNameField, "className");//默认不查给值就查
        if (StrUtil.isNotBlank(form.getSecondFormId())){
            queryWrapper.isNull(!flag, TitleFields::getType);;
        }
        return Result.OK(titleFieldsService.list(queryWrapper));

    }
    @PostMapping(PATH + "/level_three_list")
    public Result<?> levelThree(@RequestBody TitleFields form) {
        Map<String,String>maps = titleTypeService.list(new LambdaQueryWrapper<TitleType>().eq(StrUtil.isNotBlank(form.getTwoFormId()),TitleType::getTitleMenuId, form.getTwoFormId())).stream().collect(Collectors.toMap(TitleType::getId,TitleType::getName));
        LambdaQueryWrapper<TitleFields> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(StrUtil.isNotBlank(form.getTwoFormId()), TitleFields::getTitleMenuId, form.getTwoFormId())
                .ne(!StrUtil.isNotBlank(form.getIsGetEntity()), TitleFields::getNameField, "className");//默认不查给值就查
        Map<String, List<TitleFields>> collect = titleFieldsService.list(queryWrapper).stream()
                .peek(titleField -> {
                    // 如果 TitleFields::getType 返回 null，将其替换为 "默认字段"
                    String type = titleField.getType() == null ? "默认字段" : maps.get(titleField.getType());
                    titleField.setType(type); // 修改 titleField 的 type 字段
                })
                // 根据 type 字段进行排序
                .sorted(Comparator.comparing(TitleFields::getType, Comparator.nullsFirst(String::compareTo)))
                // 按照 type 字段分组，使用 LinkedHashMap 保持插入顺序
                .collect(Collectors.groupingBy(TitleFields::getType, LinkedHashMap::new, Collectors.toList()));


        return Result.OK(collect);

    }

    @PostMapping(PATH + "/title_field_map")
    public Result<?> fieldMap(@RequestBody TitleFields form) {

        return Result.OK(titleFieldsService.list().stream().collect(Collectors.toMap(TitleFields::getId, TitleFields::getName)));
    }
    @PostMapping(PATH + "/title_type_map")
    public Result<?> typeMap(@RequestBody TitleFields form) {

        return Result.OK(titleTypeService.list().stream().collect(Collectors.toMap(TitleType::getId, TitleType::getName)));
    }
    @PostMapping(PATH + "/title_menu_map")
    public Result<?> menuMap(@RequestBody TitleFields form) {

        return Result.OK(titleMenuService.list().stream().collect(Collectors.toMap(TitleMenu::getId, TitleMenu::getName)));
    }


    @SneakyThrows
    @Operation(summary = "分页")
    @PostMapping(value = PATH + "/field_page")
    public Result<?> page(@RequestBody TitleFields form, CustomPage<TitleFields> page, @RequestParam String menuCode) {
        if (StrUtil.isNotBlank(form.getType())){
            TitleType byId = this.titleTypeService.getById(form.getType());
            form.setNameType(byId.getName());
        }
        QueryWrapper<TitleFields> queryWrapper = new QueryWrapper<>();

        queryWrapper.lambda()
                .eq(StrUtil.isNotBlank(form.getNameType())&&!form.getNameType().equals("默认字段"), TitleFields::getType, form.getType())
                .eq(StrUtil.isNotBlank(form.getTitleMenuId()), TitleFields::getTitleMenuId, form.getTitleMenuId())
                .like(StrUtil.isNotBlank(form.getName()), TitleFields::getName, form.getName())
                .ne(TitleFields::getNameField, "className")
                .orderByDesc(TitleFields::getCreatedTime);
        if (StrUtil.isNotBlank(form.getIsGetType())){
            queryWrapper.lambda().isNull(TitleFields::getType);
        }
        IPage<TitleFields> result = this.titleFieldsService.page(page, queryWrapper);
        result.getRecords().forEach(d -> {
            if (StrUtil.isBlank(d.getType())) {
                d.setType("默认字段");
            }
        });

        return Result.OK(result);
    }
    @Operation(summary = "跟据id更新")
    @PostMapping(value = PATH + "/update_field_status")
    public Result<?> updateFieldStatus(@RequestBody FieldUpdateDto form) {
        if (StrUtil.isBlank(form.getMenuId())){
            return Result.error("menuId为空");
        }
        updateFieldStatusFun(form);
        return Result.OK("成功");
    }
    @Transactional(rollbackFor = Exception.class)
    public void updateFieldStatusFun(FieldUpdateDto form) {
        List<TitleFields> titleFields = titleFieldsService.list(new LambdaQueryWrapper<TitleFields>().in(TitleFields::getId, form.getFieldIds()));
        List<String> ids = titleFields
                .stream().filter(s->!s.getIsDeduplicate().equals("2")).map(TitleFields::getId).collect(Collectors.toList());

        UpdateWrapper<TitleFields>updateWrapper=new UpdateWrapper<>();
        updateWrapper.lambda().eq(TitleFields::getTitleMenuId,form.getMenuId())
                .set(TitleFields::getIsDeduplicate,"0").eq(TitleFields::getIsDeduplicate,"1");
        titleFieldsService.update(updateWrapper);
        updateWrapper.clear();


        if (CollUtil.isNotEmpty(ids)) {
            updateWrapper.lambda().in(TitleFields::getId,ids)
                    .set(TitleFields::getIsDeduplicate,"1");
            titleFieldsService.update(updateWrapper);
        }
    }


}
