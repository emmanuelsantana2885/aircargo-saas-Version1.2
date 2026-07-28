package com.aircargo.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.aircargo.authservice", "com.aircargo.common"})
@EntityScan(basePackages = {"com.aircargo.authservice.entity", "com.aircargo.common.entity"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
