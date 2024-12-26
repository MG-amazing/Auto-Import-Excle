package com.example.excel.custom;


import org.springframework.beans.factory.annotation.Autowired;

public class CustomBaseController<S extends CustomBaseService<T>, T> {
    @Autowired
    protected  S service;

}