package com.hubdelivery.auth.domain.type;

import com.hubdelivery.common.security.UserRole;

public enum RequestedRole {
    COMPANY_MANAGER(UserRole.COMPANY_MANAGER),
    HUB_MANAGER(UserRole.HUB_MANAGER),
    HUB_DELIVERY_MANAGER(UserRole.DELIVERY_MANAGER),
    COMPANY_DELIVERY_MANAGER(UserRole.DELIVERY_MANAGER);

    private final UserRole userRole;

    RequestedRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public UserRole toUserRole() {
        return userRole;
    }
}
