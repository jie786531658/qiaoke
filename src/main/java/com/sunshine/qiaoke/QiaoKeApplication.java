package com.sunshine.qiaoke;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sunshine.qiaoke.mapper")
public class QiaoKeApplication {
    public static void main(String[] args) {
        SpringApplication.run(QiaoKeApplication.class, args);
    }
}
