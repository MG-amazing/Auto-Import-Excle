package com.example.excel.custom;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public class CustomBaseServiceImpl<M extends CustomBaseMapper<T>, T> extends ServiceImpl<M, T> implements CustomBaseService<T> {
}