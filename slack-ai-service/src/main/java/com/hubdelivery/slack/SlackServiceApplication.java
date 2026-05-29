package com.hubdelivery.slack;

import com.hubdelivery.common.config.JpaAuditingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

// 수정: scanBasePackages를 com.hubdelivery로 변경 (common-module 포함)
@SpringBootApplication(scanBasePackages = "com.hubdelivery")
@EnableFeignClients(basePackages = "com.hubdelivery.slack.infrastructure.client")
@Import(JpaAuditingConfig.class)
public class SlackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlackServiceApplication.class, args);
    }
}