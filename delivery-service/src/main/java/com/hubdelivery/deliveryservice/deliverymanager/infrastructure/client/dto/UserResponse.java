package com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.dto;

import com.hubdelivery.common.security.UserRole;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponse {

    private UUID id;
    private String username;
    private UserRole role;
    private UUID hubId;
    private UUID companyId;
}
