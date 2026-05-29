package com.hubdelivery.user.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "integration.delivery-manager")
public record DeliveryManagerIntegrationProperties(
        String serviceBaseUrl
) {

    public String requiredServiceBaseUrl() {
        if (!StringUtils.hasText(serviceBaseUrl)) {
            throw new IllegalStateException("delivery-service base-url 설정이 필요합니다.");
        }

        return serviceBaseUrl.endsWith("/")
                ? serviceBaseUrl.substring(0, serviceBaseUrl.length() - 1)
                : serviceBaseUrl;
    }
}
