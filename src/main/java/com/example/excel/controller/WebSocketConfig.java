package com.example.excel.controller;

import com.example.excel.socket.ImportExcelHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {


    private final ImportExcelHandler importExcelHandler;

    public WebSocketConfig(ImportExcelHandler importExcelHandler) {
        this.importExcelHandler = importExcelHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // 注册 WebSocket 处理器，并指定 WebSocket URL（比如 "/chat"）
        registry.addHandler(importExcelHandler, "/webSocket/dynamic_import")
                .setAllowedOrigins("*");  // 设置允许跨域的源，可以根据实际需要设置
    }
}
