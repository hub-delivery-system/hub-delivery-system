package com.hubdelivery.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.repository.UserRepository;
import com.hubdelivery.user.domain.type.UserStatus;
import com.hubdelivery.user.presentation.dto.request.UserApproveRequest;
import com.hubdelivery.user.presentation.dto.response.UserApproveResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserApproveResponse approve(UUID userId, UserApproveRequest request) {

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CommonException(CommonErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 사용자입니다."));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "PENDING 상태 사용자만 승인할 수 있습니다.");
        }

        validateRoleForRequestedRole(user.getRequestedRole(), request.role());
        user.approve(request.role(), user.getHubId(), user.getCompanyId());

        // TODO: 승인 시 Keycloak 사용자 생성/활성화 + role 부여
        // keycloakAdminClient.provisionAndGrantRole(user, request.role());

        return new UserApproveResponse(
                user.getId(),
                user.getStatus(),
                user.getRole(),
                user.getHubId(),
                user.getCompanyId()
        );

    }

    private void validateRoleForRequestedRole(RequestedRole requestedRole, UserRole role) {
        if (requestedRole == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "요청 역할 정보가 없습니다.");
        }

        // MASTER라면 이미 role이 MASTER로 저장되어있음
        if (role == UserRole.MASTER) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "MASTER는 승인 API로 부여할 수 없습니다.");
        }

        UserRole expectedRole = requestedRole.toUserRole();
        if (role != expectedRole) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "요청 역할과 승인 역할이 일치하지 않습니다.");
        }
    }

}
