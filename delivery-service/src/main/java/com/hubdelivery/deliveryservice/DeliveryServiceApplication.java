package com.hubdelivery.deliveryservice;

import com.hubdelivery.common.config.JpaAuditingConfig;
import com.hubdelivery.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableFeignClients
@Import({JpaAuditingConfig.class, GlobalExceptionHandler.class})
public class DeliveryServiceApplication {

	public static void main (String[] args) {
		SpringApplication.run(DeliveryServiceApplication.class, args);
	}

}
