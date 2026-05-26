package com.hubdelivery.auth.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubdelivery.common.security.UserRole;

public record LoginResponse (

        @JsonProperty("access_token") String accessToken,
        String username,
        UserRole role

) {}
