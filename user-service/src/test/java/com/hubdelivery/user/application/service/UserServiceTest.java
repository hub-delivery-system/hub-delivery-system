package com.hubdelivery.user.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.domain.repository.UserRepository;
import com.hubdelivery.user.domain.type.UserStatus;
import com.hubdelivery.user.presentation.dto.request.UserUpdateRequest;
import com.hubdelivery.user.presentation.dto.response.UserApproveResponse;
import com.hubdelivery.user.presentation.dto.response.UserResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAffiliationResolverService userAffiliationResolverService;

    @Mock
    private UserProvisioningService userProvisioningService;

    @Mock
    private DeliveryManagerProvisionService deliveryManagerProvisionService;

    @InjectMocks
    private UserService userService;

    @Test
    void getUsers_success_appliesPagingSortingAndFilters() {
        UUID hubId = UUID.randomUUID();
        User user = User.createPending(
                "user01",
                "slack01",
                "encoded-password",
                RequestedRole.HUB_MANAGER,
                "대구허브",
                UserRole.HUB_MANAGER
        );
        user.approve(UserRole.HUB_MANAGER, hubId, null);

        when(userRepository.searchUsers(
                eq("user"),
                eq("name"),
                eq(UserRole.HUB_MANAGER),
                eq(UserStatus.APPROVED),
                eq(hubId),
                eq(null),
                any(Pageable.class)
        )).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(6, Pageable.class);
            return new PageImpl<>(List.of(user), pageable, 1);
        });

        PageResponse<UserResponse> response = userService.getUsers(
                0, 30, "updatedAt,ASC",
                "user", "name",
                UserRole.HUB_MANAGER, UserStatus.APPROVED,
                hubId, null
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).searchUsers(
                eq("user"),
                eq("name"),
                eq(UserRole.HUB_MANAGER),
                eq(UserStatus.APPROVED),
                eq(hubId),
                eq(null),
                pageableCaptor.capture()
        );
        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(30, pageable.getPageSize());
        assertTrue(pageable.getSort().getOrderFor("updatedAt").isAscending());

        assertEquals(0, response.page());
        assertEquals(30, response.size());
        assertEquals(1, response.totalElements());
        assertEquals("updatedAt, ASC", response.sort());
        assertEquals(1, response.content().size());
    }

    @Test
    void getUsers_whenSortFieldInvalid_throwsCommonException() {
        CommonException exception = assertThrows(
                CommonException.class,
                () -> userService.getUsers(0, 10, "username,ASC", null, null, null, null, null, null)
        );

        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("createdAt,updatedAt"));
    }

    @Test
    void getUsers_whenSortDirectionInvalid_throwsCommonException() {
        CommonException exception = assertThrows(
                CommonException.class,
                () -> userService.getUsers(0, 10, "createdAt,UP", null, null, null, null, null, null)
        );

        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("ASC 또는 DESC"));
    }

    @Test
    void approve_success_resolvesOrganizationAndProvisionsUser() {
        UUID userId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();
        User pendingUser = User.createPending(
                "user01",
                "slack01",
                "encoded-password",
                RequestedRole.HUB_DELIVERY_MANAGER,
                "대구허브",
                UserRole.DELIVERY_MANAGER
        );

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(pendingUser));
        when(userAffiliationResolverService.resolve(RequestedRole.HUB_DELIVERY_MANAGER, "대구허브"))
                .thenReturn(new UserAffiliationResolverService.ResolvedOrganization(hubId, null));

        UserApproveResponse response = userService.approve(userId);

        verify(userProvisioningService).activateApprovedUser(pendingUser);
        verify(deliveryManagerProvisionService).provisionIfRequired(pendingUser);

        assertEquals(UserStatus.APPROVED, response.status());
        assertEquals(UserRole.DELIVERY_MANAGER, response.role());
        assertEquals(hubId, response.hubId());
        assertNull(response.companyId());
    }

    @Test
    void approve_whenUserIsNotPending_throwsCommonException() {
        UUID userId = UUID.randomUUID();
        User rejectedUser = User.createPending(
                "user01",
                "slack01",
                "encoded-password",
                RequestedRole.HUB_MANAGER,
                "대구허브",
                UserRole.HUB_MANAGER
        );
        rejectedUser.reject();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(rejectedUser));

        CommonException exception = assertThrows(CommonException.class, () -> userService.approve(userId));
        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE.getCode(), exception.getCode());

        verify(userProvisioningService, never()).activateApprovedUser(any(User.class));
        verify(deliveryManagerProvisionService, never()).provisionIfRequired(any(User.class));
    }

    @Test
    void approve_whenActivationFails_rollsBackAndRethrows() {
        UUID userId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();
        User pendingUser = User.createPending(
                "user01",
                "slack01",
                "encoded-password",
                RequestedRole.HUB_DELIVERY_MANAGER,
                "대구허브",
                UserRole.DELIVERY_MANAGER
        );

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(pendingUser));
        when(userAffiliationResolverService.resolve(RequestedRole.HUB_DELIVERY_MANAGER, "대구허브"))
                .thenReturn(new UserAffiliationResolverService.ResolvedOrganization(hubId, null));

        RuntimeException expected = new RuntimeException("activation failed");
        org.mockito.Mockito.doThrow(expected).when(userProvisioningService).activateApprovedUser(pendingUser);

        RuntimeException actual = assertThrows(RuntimeException.class, () -> userService.approve(userId));

        assertEquals(expected, actual);
        verify(userProvisioningService).rollbackApprovalActivation(pendingUser);
        verify(deliveryManagerProvisionService, never()).provisionIfRequired(any(User.class));
    }

    @Test
    void updateUser_whenManualHubIdDiffersFromResolvedValue_throwsCommonException() {
        UUID userId = UUID.randomUUID();
        UUID resolvedHubId = UUID.randomUUID();
        UUID mismatchedHubId = UUID.randomUUID();

        User user = User.createPending(
                "user01",
                "slack01",
                "encoded-password",
                RequestedRole.HUB_MANAGER,
                "대구허브",
                UserRole.HUB_MANAGER
        );
        user.approve(UserRole.HUB_MANAGER, resolvedHubId, null);

        UserUpdateRequest request = new UserUpdateRequest(
                null,
                null,
                null,
                null,
                mismatchedHubId,
                null
        );

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userAffiliationResolverService.resolve(RequestedRole.HUB_MANAGER, "대구허브"))
                .thenReturn(new UserAffiliationResolverService.ResolvedOrganization(resolvedHubId, null));

        CommonException exception = assertThrows(CommonException.class, () -> userService.updateUser(userId, request));

        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("hubId"));
    }

    @Test
    void updateUser_whenRoleAndAffiliationChanged_updatesResolvedOrganization() {
        UUID userId = UUID.randomUUID();
        UUID newHubId = UUID.randomUUID();

        User user = User.createPending(
                "user01",
                "slack01",
                "encoded-password",
                RequestedRole.COMPANY_MANAGER,
                "대구식품",
                UserRole.COMPANY_MANAGER
        );
        user.approve(UserRole.COMPANY_MANAGER, null, UUID.randomUUID());

        UserUpdateRequest request = new UserUpdateRequest(
                "user02",
                RequestedRole.HUB_DELIVERY_MANAGER,
                UserRole.DELIVERY_MANAGER,
                "대구허브",
                null,
                null
        );

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userAffiliationResolverService.resolve(RequestedRole.HUB_DELIVERY_MANAGER, "대구허브"))
                .thenReturn(new UserAffiliationResolverService.ResolvedOrganization(newHubId, null));

        UserResponse response = userService.updateUser(userId, request);

        assertEquals("user02", response.username());
        assertEquals(RequestedRole.HUB_DELIVERY_MANAGER, response.requestedRole());
        assertEquals(UserRole.DELIVERY_MANAGER, response.role());
        assertEquals("대구허브", response.affiliationName());
        assertEquals(newHubId, response.hubId());
        assertNull(response.companyId());
    }
}
