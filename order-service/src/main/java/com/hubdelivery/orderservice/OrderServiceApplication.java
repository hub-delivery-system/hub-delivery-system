package com.hubdelivery.orderservice;

import com.hubdelivery.common.config.JpaAuditingConfig;
import com.hubdelivery.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableFeignClients                                             // Feign 클라이언트 활성화
@Import({JpaAuditingConfig.class, GlobalExceptionHandler.class}) // common-module의 Auditing·전역 예외처리 적용
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
