package com.hubdelivery.auth.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KeycloakAdminTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") Integer expiresIn,
        @JsonProperty("token_type") String tokenType
) {
}
