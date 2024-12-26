package com.example.excel.dto;

import lombok.Data;

import java.util.List;

@Data
public class FieldUpdateDto {
    private List<String> fieldIds;
    private String menuId;
}
