package com.hubdelivery;

import com.hubdelivery.common.config.JpaAuditingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.hubdelivery.hub",
        "com.hubdelivery.common",
        "com.hubdelivery.hubtohub",
        "com.hubdelivery.global"
})
@EnableDiscoveryClient
@Import(JpaAuditingConfig.class)
public class HubApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubApplication.class, args);
    }

}
