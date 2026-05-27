package com.hubdelivery.company;

import com.hubdelivery.common.config.JpaAuditingConfig;
import com.hubdelivery.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.hubdelivery.company.global.infrastructure.client")
@Import({ JpaAuditingConfig.class, GlobalExceptionHandler.class })
public class CompanyProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompanyProductServiceApplication.class, args);
	}

}
