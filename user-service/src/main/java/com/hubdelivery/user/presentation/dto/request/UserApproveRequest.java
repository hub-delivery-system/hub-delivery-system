package com.hubdelivery.user.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import com.hubdelivery.common.security.UserRole;

public record UserApproveRequest (

        @NotNull(message = "역할은 필수입니다.")
        UserRole role

) {}
