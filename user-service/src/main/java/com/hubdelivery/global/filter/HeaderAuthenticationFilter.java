package com.hubdelivery.global.filter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.repository.UserRepository;
import com.hubdelivery.user.domain.type.UserStatus;

@Component
@RequiredArgsConstructor
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String ROLE_HEADER = "X-Role";
    private static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000000";

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader(USER_ID_HEADER);
        String role = request.getHeader(ROLE_HEADER);

        if (StringUtils.hasText(userId)
                && StringUtils.hasText(role)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String authority = normalizeAuthority(role);
            if (SYSTEM_USER_ID.equals(userId) || isAuthorizedUser(userId, authority)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String normalizeAuthority(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

    private boolean isAuthorizedUser(String userId, String authority) {
        Optional<UUID> maybeUserId = parseUuid(userId);
        if (maybeUserId.isEmpty()) {
            return false;
        }

        return userRepository.findByIdAndDeletedAtIsNull(maybeUserId.get())
                .filter(this::isApproved)
                .map(User::getRole)
                .filter(role -> role != null)
                .map(Enum::name)
                .map(this::normalizeAuthority)
                .filter(authority::equals)
                .isPresent();
    }

    private boolean isApproved(User user) {
        return user.getStatus() == UserStatus.APPROVED;
    }

    private Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}
