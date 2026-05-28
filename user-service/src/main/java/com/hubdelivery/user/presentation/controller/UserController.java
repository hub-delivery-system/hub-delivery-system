package com.hubdelivery.user.presentation.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.user.application.service.UserService;
import com.hubdelivery.user.presentation.dto.request.UserApproveRequest;
import com.hubdelivery.user.presentation.dto.response.UserApproveResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PutMapping("/{userId}/approved")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    public ApiResponse<UserApproveResponse> approve(
            @PathVariable UUID userId,
            @Valid @RequestBody UserApproveRequest request
    ) {
        return ApiResponse.ok(userService.approve(userId, request));
    }

}
