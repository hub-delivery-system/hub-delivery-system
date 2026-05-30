package com.hubdelivery.user.application.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.repository.UserRepository;
import com.hubdelivery.user.domain.type.UserStatus;
import com.hubdelivery.user.presentation.dto.request.UserUpdateRequest;
import com.hubdelivery.user.presentation.dto.response.UserApproveResponse;
import com.hubdelivery.user.presentation.dto.response.UserRejectResponse;
import com.hubdelivery.user.presentation.dto.response.UserResponse;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String DEFAULT_SORT_PROPERTY = "createdAt";
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of("createdAt", "updatedAt");

    private final UserRepository userRepository;
    private final UserAffiliationResolverService userAffiliationResolverService;
    private final UserProvisioningService userProvisioningService;
    private final DeliveryManagerProvisionService deliveryManagerProvisionService;

    @Transactional
    public UserApproveResponse approve(UUID userId) {
        User user = findActiveUser(userId);

        if (user.getStatus() != UserStatus.PENDING) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "PENDING 상태 사용자만 승인할 수 있습니다.");
        }

        RequestedRole requestedRole = user.getRequestedRole();
        if (requestedRole == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "요청 역할 정보가 없습니다.");
        }

        UserRole mappedRole = requestedRole.toUserRole();
        UserAffiliationResolverService.ResolvedOrganization org =
                userAffiliationResolverService.resolve(requestedRole, user.getAffiliationName());

        UUID hubId = org.hubId();
        UUID companyId = org.companyId();

        validateOrgByRole(mappedRole, requestedRole, hubId, companyId);
        user.approve(mappedRole, hubId, companyId);

        try {
            userProvisioningService.activateApprovedUser(user);
            deliveryManagerProvisionService.provisionIfRequired(user);
        } catch (RuntimeException ex) {
            rollbackApprovalProvisioning(user, ex);
            throw ex;
        }

        return new UserApproveResponse(
                user.getId(),
                user.getStatus(),
                user.getRole(),
                user.getHubId(),
                user.getCompanyId()
        );
    }

    public PageResponse<UserResponse> getUsers(
            Integer page,
            Integer size,
            String sort,
            String username,
            String name,
            UserRole role,
            UserStatus status,
            UUID hubId,
            UUID companyId
    ) {
        Pageable pageable = createPageable(page, size, sort);
        Page<UserResponse> users = userRepository.searchUsers(
                username,
                name,
                role,
                status,
                hubId,
                companyId,
                pageable
        ).map(this::toResponse);

        return PageResponse.from(users);
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
        RequestedRole nextRequestedRole = request.requestedRole() != null ? request.requestedRole() : user.getRequestedRole();
        UserRole nextRole = request.role() != null ? request.role() : user.getRole();
        String nextAffiliationName = StringUtils.hasText(request.affiliationName()) ? request.affiliationName() : user.getAffiliationName();
        UpdatedOrganization updatedOrganization = resolveOrganizationForUpdate(
                user,
                request,
                nextRole,
                nextRequestedRole,
                nextAffiliationName
        );

        UUID nextHubId = updatedOrganization.hubId();
        UUID nextCompanyId = updatedOrganization.companyId();

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
        user.softDelete(currentActor());
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CommonException(CommonErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 사용자입니다."));
    }

    private UserResponse toResponse(User user) {
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

    private UpdatedOrganization resolveOrganizationForUpdate(
            User user,
            UserUpdateRequest request,
            UserRole nextRole,
            RequestedRole nextRequestedRole,
            String nextAffiliationName
    ) {
        if (nextRole == UserRole.MASTER) {
            if (request.hubId() != null || request.companyId() != null) {
                throw new CommonException(
                        CommonErrorCode.INVALID_INPUT_VALUE,
                        "MASTER는 hubId/companyId를 직접 입력할 수 없습니다."
                );
            }

            return new UpdatedOrganization(null, null);
        }

        boolean roleChanged = request.role() != null && request.role() != user.getRole();
        boolean requestedRoleChanged = request.requestedRole() != null && request.requestedRole() != user.getRequestedRole();
        boolean affiliationChanged = StringUtils.hasText(request.affiliationName())
                && !request.affiliationName().equals(user.getAffiliationName());
        boolean manualOrganizationProvided = request.hubId() != null || request.companyId() != null;

        UUID nextHubId = user.getHubId();
        UUID nextCompanyId = user.getCompanyId();

        if (roleChanged || requestedRoleChanged || affiliationChanged || manualOrganizationProvided) {
            UserAffiliationResolverService.ResolvedOrganization resolved =
                    userAffiliationResolverService.resolve(nextRequestedRole, nextAffiliationName);
            nextHubId = resolved.hubId();
            nextCompanyId = resolved.companyId();
        }

        validateRequestedOrganization(request.hubId(), nextHubId, "hubId");
        validateRequestedOrganization(request.companyId(), nextCompanyId, "companyId");

        if (request.hubId() != null) {
            nextHubId = request.hubId();
        }
        if (request.companyId() != null) {
            nextCompanyId = request.companyId();
        }

        if (nextRole == UserRole.HUB_MANAGER
                || nextRequestedRole == RequestedRole.HUB_DELIVERY_MANAGER) {
            nextCompanyId = null;
        } else if (nextRole == UserRole.COMPANY_MANAGER
                || nextRequestedRole == RequestedRole.COMPANY_DELIVERY_MANAGER) {
            nextHubId = null;
        }

        return new UpdatedOrganization(nextHubId, nextCompanyId);
    }

    private record UpdatedOrganization(
            UUID hubId,
            UUID companyId
    ) {
    }

    private void rollbackApprovalProvisioning(User user, RuntimeException originalException) {
        try {
            userProvisioningService.rollbackApprovalActivation(user);
        } catch (RuntimeException compensationException) {
            log.error("승인 보상 처리 실패. userId={}", user.getId(), compensationException);
            originalException.addSuppressed(compensationException);
        }
    }

    private void validateRequestedOrganization(UUID requested, UUID resolved, String fieldName) {
        if (requested != null && !requested.equals(resolved)) {
            throw new CommonException(
                    CommonErrorCode.INVALID_INPUT_VALUE,
                    fieldName + "는 affiliationName/requestedRole 조건으로 조회된 값과 일치해야 합니다."
            );
        }
    }

    private Pageable createPageable(Integer page, Integer size, String sort) {
        int pageNumber = page == null ? 0 : Math.max(page, 0);
        int pageSize = PageableUtils.validateSize(size);
        Sort resolvedSort = resolveSort(sort);

        return PageRequest.of(pageNumber, pageSize, resolvedSort);
    }

    private Sort resolveSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, DEFAULT_SORT_PROPERTY);
        }

        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        if (!StringUtils.hasText(property)) {
            property = DEFAULT_SORT_PROPERTY;
        }

        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            throw new CommonException(
                    CommonErrorCode.INVALID_INPUT_VALUE,
                    "sort는 createdAt,updatedAt만 지원합니다."
            );
        }

        Sort.Direction direction = parts.length < 2
                ? Sort.Direction.DESC
                : Sort.Direction.fromOptionalString(parts[1].trim())
                .orElseThrow(() -> new CommonException(
                        CommonErrorCode.INVALID_INPUT_VALUE,
                        "sort direction은 ASC 또는 DESC만 지원합니다."
                ));

        return Sort.by(direction, property);
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "SYSTEM";
        }

        String actor = authentication.getName();
        if (!StringUtils.hasText(actor) || "anonymousUser".equals(actor)) {
            return "SYSTEM";
        }

        return actor;
    }
}
