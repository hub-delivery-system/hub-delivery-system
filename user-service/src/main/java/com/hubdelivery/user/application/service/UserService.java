package com.hubdelivery.user.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.repository.UserRepository;
import com.hubdelivery.user.domain.type.UserStatus;
import com.hubdelivery.user.presentation.dto.request.UserApproveRequest;
import com.hubdelivery.user.presentation.dto.request.UserUpdateRequest;
import com.hubdelivery.user.presentation.dto.response.UserApproveResponse;
import com.hubdelivery.user.presentation.dto.response.UserRejectResponse;
import com.hubdelivery.user.presentation.dto.response.UserResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserApproveResponse approve(UUID userId, UserApproveRequest request) {

        User user = findActiveUser(userId);

        if (user.getStatus() != UserStatus.PENDING) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "PENDING 상태 사용자만 승인할 수 있습니다.");
        }

        validateRoleForRequestedRole(user.getRequestedRole(), request.role());
        validateOrgByRole(request.role(), user.getRequestedRole(), request.hubId(), request.companyId());

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

    // 변환된 Role(RequestedRole -> UserRole)과 저장된 Role을 비교하여 올바르게 변환되었는지 확인 (+ Role이 MASTER인 사용자의 경우에는 승인 자체는 할 수 없도록(이미 승인된 사용자이므로))
    private void validateRoleForRequestedRole(RequestedRole requestedRole, UserRole role) {
        if (requestedRole == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "요청 역할 정보가 없습니다.");
        }
        if (role == UserRole.MASTER) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "MASTER는 승인 API로 부여할 수 없습니다.");
        }
        if (requestedRole.toUserRole() != role) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "요청 역할과 승인 역할이 일치하지 않습니다.");
        }
    }

    public List<UserResponse> getUsers() {

        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUser(UUID userId) {

        return toResponse(findActiveUser(userId));

    }

    @Transactional
    public UserRejectResponse reject(UUID userId) {

        User user = findActiveUser(userId);

        if (user.getStatus() != UserStatus.PENDING) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "PENDING 상태 사용자만 거절할 수 있습니다.");
        }

        user.reject();
        return new UserRejectResponse(user.getId(), user.getStatus());
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {

        User user = findActiveUser(userId);

        String nextUsername = StringUtils.hasText(request.username()) ? request.username() : user.getUsername();
        RequestedRole nextRequestedRole = request.requestedRole() != request.requestedRole() : user.requestedRole();
        UserRole nextRole = request.role() != null ? request.role() : user.getRole();
        String nextAffiliationName = StringUtils.hasText(request.affiliationName()) ? request.affiliationName() : user.getAffiliationName();
        UUID nextHubId = request.hubId() != null ? request.hubId() : user.getHubId();
        UUID nextCompanyId = request.companyId() != null ? request.companyId() : user.getCompanyId();

        validateRoleAndRequestedRole(nextRole, nextRequestedRole);
        validateOrgByRole(nextRole, nextRequestedRole, nextHubId, nextCompanyId);

        user.updateByMaster(
                nextUsername,
                nextRequestedRole,
                nextRole,
                nextAffiliationName,
                nextHubId,
                nextCompanyId
        );

        return toResponse(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {

        User user = findActiveUser(userId);
        user.softDelete("MASTER"); // TODO: 실제 로그인 사용자로 변경

    }

    private User findActiveUser(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CommonException(CommonErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 사용자입니다."));
    }

    private UserResponse toReponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getSlackId(),
                user.getRequestedRole(),
                user.getRole(),
                user.getAffiliationName(),
                user.getStatus(),
                user.getCompanyId(),
                user.getHubId(),
                user.getCreatedAt(),
                user.getCreatedBy(),
                user.getUpdatedAt(),
                user.getUpdatedBy()
        );
    }

    private void validateRoleAndRequestedRole(UserRole role, RequestedRole requestedRole) {
        if (role == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "role은 필수입니다.");
        }

        if (role == UserRole.MASTER) {
            if (requestedRole != null) {
                throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "MASTER는 requestedRole을 가질 수 없습니다.");
            }
            return;
        }

        if (requestedRole == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "requestedRole은 필수입니다.");
        }

        if (requestedRole.toUserRole() != role) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "requestedRole과 role 매핑이 일치하지 않습니다.");
        }
    }

    private void validateOrgByRole(UserRole role, RequestedRole requestedRole, UUID hubId, UUID companyId) {
        switch (role) {
            case MASTER -> {
                if (hubId != null || companyId != null) {
                    throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "MASTER는 hubId/companyId를 가질 수 없습니다.");
                }
            }
            case HUB_MANAGER -> {
                if (hubId == null || companyId != null) {
                    throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "HUB_MANAGER는 hubId 필수, companyId는 null이어야 합니다.");
                }
            }
            case COMPANY_MANAGER -> {
                if (companyId == null || hubId != null) {
                    throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "COMPANY_MANAGER는 companyId 필수, hubId는 null이어야 합니다.");
                }
            }
            case DELIVERY_MANAGER -> {
                if (requestedRole == RequestedRole.HUB_DELIVERY_MANAGER) {
                    if (hubId == null || companyId != null) {
                        throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "HUB_DELIVERY_MANAGER는 hubId 필수, companyId는 null이어야 합니다.");
                    }
                } else if (requestedRole == RequestedRole.COMPANY_DELIVERY_MANAGER) {
                    if (companyId == null || hubId != null) {
                        throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "COMPANY_DELIVERY_MANAGER는 companyId 필수, hubId는 null이어야 합니다.");
                    }
                } else {
                    throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "DELIVERY_MANAGER는 배송계 requestedRole이어야 합니다.");
                }
            }
        }
    }


}
