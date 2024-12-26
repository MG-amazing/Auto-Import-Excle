package com.example.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultMessage {
    private String message;
    private String status;
    private String data;
    private Double percentage;

    public ResultMessage(Double percentage) {
        this.percentage = percentage;
    }
    public ResultMessage(String message) {
        this.message = message;
    }
}
