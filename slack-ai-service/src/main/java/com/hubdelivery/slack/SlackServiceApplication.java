package com.hubdelivery.slack;

import com.hubdelivery.common.config.JpaAuditingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "com.hubdelivery")
@EnableFeignClients(basePackages = "com.hubdelivery.slack.infrastructure.client")
@Import(JpaAuditingConfig.class)
@EnableScheduling
@EnableRetry
public class SlackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlackServiceApplication.class, args);
    }
}