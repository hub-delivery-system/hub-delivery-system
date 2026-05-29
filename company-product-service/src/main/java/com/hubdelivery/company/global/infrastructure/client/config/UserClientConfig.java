package com.hubdelivery.company.global.infrastructure.client.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

public class UserClientConfig {

    @Bean
    public Retryer userClientRetryer() {
        return new Retryer.Default(100, 1_000, 3);
    }
}
