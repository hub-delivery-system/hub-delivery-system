package com.hubdelivery.auth.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.repository.UserRepository;
import com.hubdelivery.user.domain.type.UserStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final KeycloakTokenClient keycloakTokenClient;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsBySlackId(request.slackId())) {
            throw new SlackIdAlreadyExistsException();
        }

        validateRequestedRole(request.requestedRole());
        UserRole mappedRole = request.requestedRole().toUserRole();

        User savedUser = userRepository.save(
                User.createPending(
                        request.username(),
                        request.slackId(),
                        request.password(),
                        request.requestedRole(),
                        request.affiliationName(),
                        mappedRole
                )
        );

        return new SignupResponse(
                savedUser.getUsername(),
                savedUser.getSlackId(),
                savedUser.getRequestedRole(),
                savedUser.getRole(),
                savedUser.getAffiliationName(),
                savedUser.getStatus(),
                savedUser.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findBySlackIdAndDeletedAtIsNull(request.slackId())
                .orElseThrow(LoginFailedException::new);

        if (user.getStatus() == UserStatus.PENDING) {
            throw new PendingApprovalException();
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            throw new RejectedUserException();
        }

        KeycloakTokenResponse token = keycloakTokenClient.issueToken(request.slackId(), request.password());
        if (token == null || token.accessToken() == null) {
            throw new LoginFailedException();
        }

        return new LoginResponse(
                token.accessToken(),
                token.tokenType(),
                user.getUsername(),
                user.getRole(),
                user.getRequestedRole()
        );
    }

    private void validateRequestedRole(RequestedRole requestedRole) {
        if (requestedRole == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "요청 역할은 필수입니다.");
        }
    }

}
