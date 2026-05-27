package com.hubdelivery.auth.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hubdelivery.auth.domain.exception.LoginFailedException;
import com.hubdelivery.auth.domain.exception.PendingApprovalException;
import com.hubdelivery.auth.domain.exception.RejectedUserException;
import com.hubdelivery.auth.domain.exception.SlackIdAlreadyExistsException;
import com.hubdelivery.auth.domain.exception.UsernameAlreadyExistsException;
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

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsBySlackIdAndDeletedAtIsNull(request.slackId())) {
            throw new SlackIdAlreadyExistsException();
        }

        // username도 유니크로 사용
        if (userRepository.existsByUsernameAndDeletedAtIsNull(request.username())) {
            throw new UsernameAlreadyExistsException();
        }

        // TODO: Keycloack 연동 후 password 저장/검증 방식 변경 필요
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

        // TODO: Keycloak 연동 후 password 검증 방식 변경 필요
        if (!user.getPassword().equals(request.password())) {
            throw new LoginFailedException();
        }

        // TODO: Keycloack token endpoint 연동 후 access token 반환
        return new LoginResponse(
                null,
                user.getUsername(),
                user.getRole()
        );
    }

}
