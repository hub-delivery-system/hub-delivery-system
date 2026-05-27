package com.hubdelivery.auth.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.security.UserRole;

public record LoginResponse (

        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        String username,
        UserRole role,
        RequestedRole requestedRole

) {}
