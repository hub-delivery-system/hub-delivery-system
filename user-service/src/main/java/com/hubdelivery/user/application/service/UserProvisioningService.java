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

    public void provisionPendingUser(User user, String rawPassword) {
        validateSlackId(user);
        if (!StringUtils.hasText(rawPassword)) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "password는 필수입니다.");
        }

        String adminAccessToken = keycloakAdminTokenClient.issueAdminAccessToken();
        String keycloakUsername = user.getSlackId();

        String keycloakUserId = keycloakAdminUserClient.findUserIdByUsername(adminAccessToken, keycloakUsername)
                .orElseGet(() -> keycloakAdminUserClient.createUser(adminAccessToken, keycloakUsername));

        keycloakAdminUserClient.disableUser(adminAccessToken, keycloakUserId, keycloakUsername);
        keycloakAdminUserClient.setPassword(adminAccessToken, keycloakUserId, rawPassword);

        log.info("Keycloak pending user provisioning completed. userId={}, keycloakUserId={}",
                user.getId(), keycloakUserId);
    }

    public void activateApprovedUser(User user) {
        validateSlackId(user);
        if (user.getRole() == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "role은 필수입니다.");
        }

        String adminAccessToken = keycloakAdminTokenClient.issueAdminAccessToken();
        String keycloakUsername = user.getSlackId();

        String keycloakUserId = keycloakAdminUserClient.findUserIdByUsername(adminAccessToken, keycloakUsername)
                .orElseThrow(() -> new CommonException(
                        CommonErrorCode.EXTERNAL_API_FAILURE,
                        "승인 대상 Keycloak 사용자를 찾을 수 없습니다. slackId=" + keycloakUsername
                ));

        keycloakAdminUserClient.enableUser(adminAccessToken, keycloakUserId, keycloakUsername);
        keycloakAdminUserClient.assignRealmRole(adminAccessToken, keycloakUserId, user.getRole().name());

        log.info("Keycloak approved user activation completed. userId={}, keycloakUserId={}, role={}",
                user.getId(), keycloakUserId, user.getRole());
    }

    public void compensateProvisioning(User user) {
        if (user == null || !StringUtils.hasText(user.getSlackId())) {
            return;
        }

        String adminAccessToken = keycloakAdminTokenClient.issueAdminAccessToken();
        String keycloakUsername = user.getSlackId();

        keycloakAdminUserClient.findUserIdByUsername(adminAccessToken, keycloakUsername)
                .ifPresent(keycloakUserId -> {
                    keycloakAdminUserClient.deleteUser(adminAccessToken, keycloakUserId);
                    log.info("Keycloak compensation completed. userId={}, keycloakUserId={}",
                            user.getId(), keycloakUserId);
                });
    }

    public void rollbackApprovalActivation(User user) {
        if (user == null || !StringUtils.hasText(user.getSlackId())) {
            return;
        }

        String adminAccessToken = keycloakAdminTokenClient.issueAdminAccessToken();
        String keycloakUsername = user.getSlackId();

        keycloakAdminUserClient.findUserIdByUsername(adminAccessToken, keycloakUsername)
                .ifPresent(keycloakUserId -> {
                    keycloakAdminUserClient.disableUser(adminAccessToken, keycloakUserId, keycloakUsername);
                    log.info("Keycloak approval rollback completed. userId={}, keycloakUserId={}",
                            user.getId(), keycloakUserId);
                });
    }

    private void validateSlackId(User user) {
        if (user == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "승인 사용자 정보가 없습니다.");
        }
        if (!StringUtils.hasText(user.getSlackId())) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "slackId는 필수입니다.");
        }
    }
}
