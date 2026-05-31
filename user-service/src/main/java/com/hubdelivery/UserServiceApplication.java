package com.hubdelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;

import com.hubdelivery.common.config.JpaAuditingConfig;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
@Import(JpaAuditingConfig.class)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
