package com.hubdelivery.user.presentation.dto.response;

import java.util.UUID;

import com.hubdelivery.user.domain.type.UserStatus;

public record UserRejectResponse(

        UUID userId,
        UserStatus status

) {

}
