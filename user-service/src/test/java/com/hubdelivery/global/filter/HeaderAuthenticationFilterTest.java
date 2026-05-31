package com.hubdelivery.global.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class HeaderAuthenticationFilterTest {

    private static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000000";

    @Mock
    private UserRepository userRepository;

    private HeaderAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HeaderAuthenticationFilter(userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_setsAuthentication_whenApprovedUserAndRoleMatches() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.createPending(
                "user01",
                "slack01",
                "encoded-password",
                RequestedRole.HUB_MANAGER,
                "대구허브",
                UserRole.HUB_MANAGER
        );
        user.approve(UserRole.HUB_MANAGER, UUID.randomUUID(), null);

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
        request.addHeader("X-User-Id", userId.toString());
        request.addHeader("X-Role", "HUB_MANAGER");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userId.toString(), authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_HUB_MANAGER".equals(authority.getAuthority())));
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }

    @Test
    void doFilter_skipsAuthentication_whenRoleDoesNotMatchDbRole() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.createPending(
                "user01",
                "slack01",
                "encoded-password",
                RequestedRole.HUB_MANAGER,
                "대구허브",
                UserRole.HUB_MANAGER
        );
        user.approve(UserRole.HUB_MANAGER, UUID.randomUUID(), null);

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
        request.addHeader("X-User-Id", userId.toString());
        request.addHeader("X-Role", "COMPANY_MANAGER");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }

    @Test
    void doFilter_setsAuthentication_forSystemUserWithoutDbLookup() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
        request.addHeader("X-User-Id", SYSTEM_USER_ID);
        request.addHeader("X-Role", "MASTER");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(SYSTEM_USER_ID, authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_MASTER".equals(authority.getAuthority())));
        verifyNoInteractions(userRepository);
    }
}
