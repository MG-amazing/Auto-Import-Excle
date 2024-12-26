package com.example.excel.custom;



public class CustomBaseController<S extends CustomBaseService<T>, T> {
    private final S service;

    public CustomBaseController(S service) {
        this.service = service;
    }
}