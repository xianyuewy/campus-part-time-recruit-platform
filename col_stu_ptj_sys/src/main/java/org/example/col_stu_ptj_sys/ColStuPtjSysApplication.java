package org.example.col_stu_ptj_sys;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ColStuPtjSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColStuPtjSysApplication.class, args);
    }

}
