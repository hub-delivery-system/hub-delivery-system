package com.hubdelivery.orderservice.order.infrastructure.client.user.dto;

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
    private UUID hubId;      // HUB_MANAGER 권한 체크 시 사용
    private UUID companyId;
}
