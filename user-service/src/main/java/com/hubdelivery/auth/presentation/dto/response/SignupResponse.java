package com.hubdelivery.auth.presentation.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubdelivery.user.domain.type.UserStatus;

public record SignupResponse (

        String username,
        @JsonProperty("slack_id") String slackId,
        @JsonProperty("affiliation_type") String affiliationType,
        @JsonProperty("affiliation_name") String affiliationName,
        UserStatus status,
        @JsonProperty("created_at") LocalDateTime createdAt

) {}
