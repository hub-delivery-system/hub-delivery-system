package com.hubdelivery.company.global.infrastructure.client.user.dto;

import com.hubdelivery.common.security.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String slackId,
        UserRole role,
        String status,
        UUID companyId,
        UUID hubId
) {
}
