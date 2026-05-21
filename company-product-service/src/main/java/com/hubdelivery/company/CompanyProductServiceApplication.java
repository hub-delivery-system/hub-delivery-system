package com.hubdelivery.company;

import com.hubdelivery.common.config.JpaAuditingConfig;
import com.hubdelivery.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
// TODO: Feign client 구현 시 @EnableFeignClients 를 추가
@Import({ JpaAuditingConfig.class, GlobalExceptionHandler.class })
public class CompanyProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompanyProductServiceApplication.class, args);
	}

}
