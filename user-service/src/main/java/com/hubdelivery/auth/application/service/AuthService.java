package com.hubdelivery.auth.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hubdelivery.auth.domain.exception.LoginFailedException;
import com.hubdelivery.auth.domain.exception.PendingApprovalException;
import com.hubdelivery.auth.domain.exception.RejectedUserException;
import com.hubdelivery.auth.domain.exception.SlackIdAlreadyExistsException;
import com.hubdelivery.auth.infrastructure.client.KeycloakTokenClient;
import com.hubdelivery.auth.infrastructure.client.KeycloakTokenResponse;
import com.hubdelivery.auth.presentation.dto.request.LoginRequest;
import com.hubdelivery.auth.presentation.dto.request.SignupRequest;
import com.hubdelivery.auth.presentation.dto.response.LoginResponse;
import com.hubdelivery.auth.presentation.dto.response.SignupResponse;
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

        if (userRepository.existsBySlackIdAndDeletedAtIsNull(request.slackId())) {
            throw new SlackIdAlreadyExistsException();
        }

        User savedUser = userRepository.save(
                User.createPending(
                        request.username(),
                        request.slackId(),
                        request.password(),
                        request.affiliationType(),
                        request.affiliationName()
                )
        );

        return new SignupResponse(
                savedUser.getUsername(),
                savedUser.getSlackId(),
                savedUser.getAffiliationType().name(),
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
                user.getUsername(),
                user.getRole()
        );
    }

}
