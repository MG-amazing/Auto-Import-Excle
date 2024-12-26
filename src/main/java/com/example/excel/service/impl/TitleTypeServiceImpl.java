package com.example.excel.service.impl;

import com.example.excel.custom.CustomBaseServiceImpl;
import com.example.excel.entity.TitleType;
import com.example.excel.mapper.TitleTypeMapper;
import com.example.excel.service.TitleTypeService;
import org.springframework.stereotype.Service;


@Service
public class TitleTypeServiceImpl  extends CustomBaseServiceImpl<TitleTypeMapper, TitleType> implements TitleTypeService {

}
