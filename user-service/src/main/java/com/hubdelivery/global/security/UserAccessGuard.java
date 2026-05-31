package com.hubdelivery.global.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userAccessGuard")
public class UserAccessGuard {

    public boolean isSelf(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        return userId.toString().equals(authentication.getName());
    }
}
