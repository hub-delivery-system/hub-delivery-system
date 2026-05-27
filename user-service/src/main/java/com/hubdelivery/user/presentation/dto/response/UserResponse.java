package com.hubdelivery.user.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.type.UserStatus;

public record UserResponse(

        UUID id,
        String username,
        @JsonProperty("slack_id") String slackId,
        RequestedRole requestedRole,
        UserRole role,
        @JsonProperty("affiliation_name") String affiliationName,
        UserStatus status,
        UUID companyId,
        UUID hubId,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("created_by") String createdBy,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        @JsonProperty("updated_by") String updatedBy

) {}
