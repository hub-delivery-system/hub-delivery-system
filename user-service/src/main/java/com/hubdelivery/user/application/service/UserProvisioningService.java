package com.hubdelivery.user.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hubdelivery.auth.infrastructure.client.KeycloakAdminTokenClient;
import com.hubdelivery.auth.infrastructure.client.KeycloakAdminUserClient;
import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.user.domain.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final KeycloakAdminTokenClient keycloakAdminTokenClient;
    private final KeycloakAdminUserClient keycloakAdminUserClient;

    public void provisionApprovedUser(User user) {
        validate(user);

        String adminAccessToken = keycloakAdminTokenClient.issueAdminAccessToken();
        String keycloakUsername = user.getSlackId();

        String keycloakUserId = keycloakAdminUserClient.findUserIdByUsername(adminAccessToken, keycloakUsername)
                .orElseGet(() -> keycloakAdminUserClient.createUser(adminAccessToken, keycloakUsername));

        keycloakAdminUserClient.enableUser(adminAccessToken, keycloakUserId);
        keycloakAdminUserClient.setPassword(adminAccessToken, keycloakUserId, user.getPassword());
        keycloakAdminUserClient.assignRealmRole(adminAccessToken, keycloakUserId, user.getRole().name());

        log.info("Keycloak provisioning completed. userId={}, keycloakUserId={}, role={}",
                user.getId(), keycloakUserId, user.getRole());
    }

    private void validate(User user) {
        if (user == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "승인 사용자 정보가 없습니다.");
        }
        if (!StringUtils.hasText(user.getSlackId())) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "slackId는 필수입니다.");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "password는 필수입니다.");
        }
        if (user.getRole() == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "role은 필수입니다.");
        }
    }
}
