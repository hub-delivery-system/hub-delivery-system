package com.hubdelivery.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.repository.UserRepository;
import com.hubdelivery.user.domain.type.AffiliationType;
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

        // TODO: affiliation_name 기반으로 hubId/companyId 조회 연동
        // 현재는 가입 당시 값 유지 (없으면 null)
        validateRoleForAffiliation(user.getAffiliationType(), request.role());

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

    private void validateRoleForAffiliation(AffiliationType affiliationType, UserRole role) {

        if (affiliationType == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "소속 타입이 없습니다.");
        }

        if (role == UserRole.MASTER) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "MASTER는 승인 API로 부여할 수 없습니다.");
        }

        switch (affiliationType) {
            case HUB -> {
                if (role == UserRole.COMPANY_MANAGER) {
                    throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "HUB 소속은 COMPANY_MANAGER를 부여할 수 없습니다.");
                }
            }
            case COMPANY -> {
                if (role == UserRole.HUB_MANAGER || role == UserRole.DELIVERY_MANAGER) {
                    throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "COMPANY 소속은 HUB/DELIVERY 권한을 부여할 수 없습니다.");
                }
            }
        }

    }

}
