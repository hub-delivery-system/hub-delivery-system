package com.hubdelivery.user.domain.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.type.UserStatus;

public interface UserRepositoryCustom {

    Page<User> searchUsers(
            String username,
            String name,
            UserRole role,
            UserStatus status,
            UUID hubId,
            UUID companyId,
            Pageable pageable
    );
}
