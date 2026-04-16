package com.point.pointaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.point.pointaicodemother.mapper")
public class PointAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointAiCodeMotherApplication.class, args);
    }

}
