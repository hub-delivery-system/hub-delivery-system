package com.hubdelivery.company.global.infrastructure.client.hub.config;

import org.springframework.context.annotation.Bean;

import feign.Retryer;

public class HubClientConfig {

    @Bean
    public Retryer hubClientRetryer() {
        return new Retryer.Default(100, 1_000, 3);
    }
}
