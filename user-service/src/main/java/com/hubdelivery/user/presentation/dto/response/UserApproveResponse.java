package com.hubdelivery.user.presentation.dto.response;

import java.util.UUID;

import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.type.UserStatus;

public record UserApproveResponse(

        UUID userId,
        UserStatus status,
        UserRole role,
        UUID hubId,
        UUID companyId

) {}
