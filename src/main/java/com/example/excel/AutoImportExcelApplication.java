package com.example.excel;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
@EnableCaching//开启SpringCache注解功能
@MapperScan("com.example.excel.mapper")  // 确保扫描到 Mapper 包
public class AutoImportExcelApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoImportExcelApplication.class, args);
    }


}
