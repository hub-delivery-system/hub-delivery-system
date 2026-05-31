package com.hubdelivery.user.presentation.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.security.UserRole;

public record UserUpdateRequest(

        @Size(min = 4, max = 10, message = "username은 4~10자여야 합니다.")
        @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "username 형식이 올바르지 않습니다.")
        String username,

        @JsonProperty("requested_role")
        RequestedRole requestedRole,

        UserRole role,

        @JsonProperty("affiliation_name")
        @Size(max = 100, message = "affiliationName은 100자 이하여야 합니다.")
        String affiliationName,

        @JsonProperty("hub_id")
        UUID hubId,

        @JsonProperty("company_id")
        UUID companyId

) {

}
