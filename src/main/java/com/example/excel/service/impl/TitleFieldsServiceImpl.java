package com.example.excel.service.impl;

import com.example.excel.custom.CustomBaseServiceImpl;
import com.example.excel.entity.TitleFields;
import com.example.excel.mapper.TitleFieldsMapper;
import com.example.excel.service.TitleFieldsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@Slf4j
public class TitleFieldsServiceImpl extends CustomBaseServiceImpl<TitleFieldsMapper, TitleFields> implements TitleFieldsService {

}