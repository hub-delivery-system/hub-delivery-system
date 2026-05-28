package com.hubdelivery.user.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubdelivery.common.security.UserRole;

public record UserApproveRequest (

        @NotNull(message = "역할은 필수입니다.")
        UserRole role,

        @JsonProperty("hub_id") UUID hubId,

        @JsonProperty("company_id") UUID companyId

) {}
