package com.hubdelivery.user.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.type.UserStatus;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> withFilters(
            String username,
            String name,
            UserRole role,
            UserStatus status,
            UUID hubId,
            UUID companyId
    ) {
        return Specification.allOf(
                usernameContains(username),
                usernameContains(name),
                roleEquals(role),
                statusEquals(status),
                hubIdEquals(hubId),
                companyIdEquals(companyId)
        );
    }

    private static Specification<User> usernameContains(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String keyword = "%" + value.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("username")), keyword);
    }

    private static Specification<User> roleEquals(UserRole role) {
        if (role == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    private static Specification<User> statusEquals(UserStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<User> hubIdEquals(UUID hubId) {
        if (hubId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("hubId"), hubId);
    }

    private static Specification<User> companyIdEquals(UUID companyId) {
        if (companyId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }
}
