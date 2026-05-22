package com.hubdelivery.hub.domain.type;

// common-module/src/main/java/com/hubdelivery/common/domain/type/UserRole.java

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    MASTER("마스터 관리자"),
    HUB_MANAGER("허브 관리자"),
    DELIVERY_MANAGER("배송 담당자"),
    COMPANY_MANAGER("업체 관리자");

    private final String description;

    // Spring Security에서 ROLE_ prefix 자동 추가하므로 그대로 사용
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    public static UserRole fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        // ROLE_ prefix 제거
        String cleanRole = role.startsWith("ROLE_") ? role.substring(5) : role;

        try {
            return UserRole.valueOf(cleanRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
