package com.hubdelivery.auth.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "keycloak.admin")
public record KeycloakAdminProperties(
        String realm,
        String clientId,
        String clientSecret,
        String username,
        String password
) {

    public String resolvedRealm() {
        return StringUtils.hasText(realm) ? realm : "master";
    }

    public String resolvedClientId() {
        return StringUtils.hasText(clientId) ? clientId : "admin-cli";
    }

    public boolean hasClientSecret() {
        return StringUtils.hasText(clientSecret);
    }

    public boolean isCredentialConfigured() {
        return StringUtils.hasText(username) && StringUtils.hasText(password);
    }
}

