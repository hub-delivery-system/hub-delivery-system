package com.hubdelivery.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hubdelivery.auth.domain.exception.LoginFailedException;
import com.hubdelivery.auth.domain.exception.PendingApprovalException;
import com.hubdelivery.auth.domain.exception.RejectedUserException;
import com.hubdelivery.auth.domain.exception.SlackIdAlreadyExistsException;
import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.auth.infrastructure.client.KeycloakTokenClient;
import com.hubdelivery.auth.infrastructure.client.KeycloakTokenResponse;
import com.hubdelivery.auth.presentation.dto.request.LoginRequest;
import com.hubdelivery.auth.presentation.dto.request.SignupRequest;
import com.hubdelivery.auth.presentation.dto.response.LoginResponse;
import com.hubdelivery.auth.presentation.dto.response.SignupResponse;
import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.application.service.UserProvisioningService;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.repository.UserRepository;
import com.hubdelivery.user.domain.type.UserStatus;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakTokenClient keycloakTokenClient;

    @Mock
    private UserProvisioningService userProvisioningService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_success_savesEncodedPassword_andReturnsPendingResponse() {
        SignupRequest request = new SignupRequest(
                "slack01",
                "Password1!",
                "user01",
                "대구허브",
                RequestedRole.HUB_MANAGER
        );

        when(userRepository.existsBySlackId(request.slackId())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SignupResponse response = authService.signup(request);

        ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUserCaptor.capture());
        User savedUser = savedUserCaptor.getValue();

        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(UserRole.HUB_MANAGER, savedUser.getRole());
        assertEquals(UserStatus.PENDING, savedUser.getStatus());

        verify(userProvisioningService).provisionPendingUser(savedUser, request.password());

        assertEquals(request.username(), response.username());
        assertEquals(request.slackId(), response.slackId());
        assertEquals(request.requestedRole(), response.requestedRole());
        assertEquals(UserRole.HUB_MANAGER, response.role());
        assertEquals(UserStatus.PENDING, response.status());
    }

    @Test
    void signup_whenSlackIdAlreadyExists_throwsException() {
        SignupRequest request = new SignupRequest(
                "duplicated-slack",
                "Password1!",
                "user01",
                "대구허브",
                RequestedRole.HUB_MANAGER
        );

        when(userRepository.existsBySlackId(request.slackId())).thenReturn(true);

        assertThrows(SlackIdAlreadyExistsException.class, () -> authService.signup(request));
        verify(userRepository, never()).save(any(User.class));
        verify(userProvisioningService, never()).provisionPendingUser(any(User.class), any(String.class));
    }

    @Test
    void signup_whenRequestedRoleMissing_throwsCommonException() {
        SignupRequest request = new SignupRequest(
                "slack01",
                "Password1!",
                "user01",
                "대구허브",
                null
        );

        when(userRepository.existsBySlackId(request.slackId())).thenReturn(false);

        CommonException exception = assertThrows(CommonException.class, () -> authService.signup(request));
        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE.getCode(), exception.getCode());
    }

    @Test
    void signup_whenProvisioningFails_runsCompensation_andRethrows() {
        SignupRequest request = new SignupRequest(
                "slack01",
                "Password1!",
                "user01",
                "대구허브",
                RequestedRole.HUB_MANAGER
        );

        when(userRepository.existsBySlackId(request.slackId())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RuntimeException expected = new RuntimeException("keycloak failed");
        org.mockito.Mockito.doThrow(expected)
                .when(userProvisioningService)
                .provisionPendingUser(any(User.class), eq(request.password()));

        RuntimeException actual = assertThrows(RuntimeException.class, () -> authService.signup(request));

        assertEquals(expected, actual);
        verify(userProvisioningService).compensateProvisioning(any(User.class));
    }

    @Test
    void login_whenUserNotFound_throwsLoginFailedException() {
        LoginRequest request = new LoginRequest("unknown", "Password1!");
        when(userRepository.findBySlackIdAndDeletedAtIsNull(request.slackId())).thenReturn(Optional.empty());

        assertThrows(LoginFailedException.class, () -> authService.login(request));
        verify(keycloakTokenClient, never()).issueToken(any(String.class), any(String.class));
    }

    @Test
    void login_whenPendingUser_throwsPendingApprovalException() {
        LoginRequest request = new LoginRequest("slack01", "Password1!");
        User pendingUser = createPendingUser("slack01", RequestedRole.HUB_MANAGER, UserRole.HUB_MANAGER);

        when(userRepository.findBySlackIdAndDeletedAtIsNull(request.slackId())).thenReturn(Optional.of(pendingUser));

        assertThrows(PendingApprovalException.class, () -> authService.login(request));
        verify(keycloakTokenClient, never()).issueToken(any(String.class), any(String.class));
    }

    @Test
    void login_whenRejectedUser_throwsRejectedUserException() {
        LoginRequest request = new LoginRequest("slack01", "Password1!");
        User rejectedUser = createPendingUser("slack01", RequestedRole.HUB_MANAGER, UserRole.HUB_MANAGER);
        rejectedUser.reject();

        when(userRepository.findBySlackIdAndDeletedAtIsNull(request.slackId())).thenReturn(Optional.of(rejectedUser));

        assertThrows(RejectedUserException.class, () -> authService.login(request));
        verify(keycloakTokenClient, never()).issueToken(any(String.class), any(String.class));
    }

    @Test
    void login_whenTokenMissing_throwsLoginFailedException() {
        LoginRequest request = new LoginRequest("slack01", "Password1!");
        User approvedUser = createPendingUser("slack01", RequestedRole.HUB_MANAGER, UserRole.HUB_MANAGER);
        approvedUser.approve(UserRole.HUB_MANAGER, UUID.randomUUID(), null);

        when(userRepository.findBySlackIdAndDeletedAtIsNull(request.slackId())).thenReturn(Optional.of(approvedUser));
        when(keycloakTokenClient.issueToken(request.slackId(), request.password()))
                .thenReturn(new KeycloakTokenResponse(null, 300, "Bearer", "refresh"));

        assertThrows(LoginFailedException.class, () -> authService.login(request));
    }

    @Test
    void login_success_returnsAccessTokenResponse() {
        LoginRequest request = new LoginRequest("slack01", "Password1!");
        User approvedUser = createPendingUser("slack01", RequestedRole.HUB_MANAGER, UserRole.HUB_MANAGER);
        approvedUser.approve(UserRole.HUB_MANAGER, UUID.randomUUID(), null);

        when(userRepository.findBySlackIdAndDeletedAtIsNull(request.slackId())).thenReturn(Optional.of(approvedUser));
        when(keycloakTokenClient.issueToken(request.slackId(), request.password()))
                .thenReturn(new KeycloakTokenResponse("access-token", 300, "Bearer", "refresh-token"));

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("user01", response.username());
        assertEquals(UserRole.HUB_MANAGER, response.role());
        assertEquals(RequestedRole.HUB_MANAGER, response.requestedRole());
    }

    private User createPendingUser(String slackId, RequestedRole requestedRole, UserRole mappedRole) {
        return User.createPending(
                "user01",
                slackId,
                "encoded-password",
                requestedRole,
                "대구허브",
                mappedRole
        );
    }
}
