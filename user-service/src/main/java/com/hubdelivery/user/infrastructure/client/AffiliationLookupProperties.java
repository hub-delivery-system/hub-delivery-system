package com.hubdelivery.user.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "integration.affiliation")
public record AffiliationLookupProperties(
        String hubServiceBaseUrl,
        String companyServiceBaseUrl
) {

    public String requiredHubServiceBaseUrl() {
        return requireBaseUrl(hubServiceBaseUrl, "hub-service");
    }

    public String requiredCompanyServiceBaseUrl() {
        return requireBaseUrl(companyServiceBaseUrl, "company-product-service");
    }

    private String requireBaseUrl(String baseUrl, String serviceName) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalStateException(serviceName + " base-url 설정이 필요합니다.");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
