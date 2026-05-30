package com.hubdelivery.user.presentation.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.user.application.service.UserService;
import com.hubdelivery.user.presentation.dto.request.UserUpdateRequest;
import com.hubdelivery.user.presentation.dto.response.UserApproveResponse;
import com.hubdelivery.user.presentation.dto.response.UserRejectResponse;
import com.hubdelivery.user.presentation.dto.response.UserResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PatchMapping("/{userId}/approve")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ApiResponse<UserApproveResponse> approve(
            @PathVariable UUID userId
    ) {
        return ApiResponse.ok(userService.approve(userId));
    }

    @PatchMapping("/{userId}/reject")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ApiResponse<UserRejectResponse> reject(
            @PathVariable UUID userId
    ) {
        return ApiResponse.ok(userService.reject(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(userService.getUsers(page, size, sort));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER') or @userAccessGuard.isSelf(#userId)")
    public ApiResponse<UserResponse> getUser(
            @PathVariable UUID userId
    ) {
        return ApiResponse.ok(userService.getUser(userId));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ApiResponse.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<Void> deleteUser(
            @PathVariable UUID userId

    ) {
        userService.deleteUser(userId);
        return ApiResponse.ok(null);
    }

}
