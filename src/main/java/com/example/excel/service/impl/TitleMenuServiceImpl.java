package com.example.excel.service.impl;

import com.example.excel.custom.CustomBaseServiceImpl;
import com.example.excel.entity.TitleMenu;
import com.example.excel.mapper.TitleMenuMapper;
import com.example.excel.service.TitleMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@Slf4j
public class TitleMenuServiceImpl extends CustomBaseServiceImpl<TitleMenuMapper, TitleMenu> implements TitleMenuService {

}
