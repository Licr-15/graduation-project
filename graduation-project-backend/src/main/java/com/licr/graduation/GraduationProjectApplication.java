package com.licr.graduation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.licr.graduation.mapper")
public class GraduationProjectApplication {
    public static void main(String[] args) { SpringApplication.run(GraduationProjectApplication.class, args); }
}
