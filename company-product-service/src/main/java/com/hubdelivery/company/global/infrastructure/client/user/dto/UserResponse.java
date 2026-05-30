package com.hubdelivery.company.global.infrastructure.client.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hubdelivery.common.security.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        @JsonAlias("slack_id")
        String slackId,
        UserRole role,
        String status,
        @JsonAlias("company_id")
        UUID companyId,
        @JsonAlias("hub_id")
        UUID hubId
) {
}
