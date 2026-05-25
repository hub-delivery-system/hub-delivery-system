package com.hubdelivery.auth.presentation.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.hubdelivery.auth.application.service.AuthService;
import com.hubdelivery.auth.presentation.dto.request.LoginRequest;
import com.hubdelivery.auth.presentation.dto.request.SignupRequest;
import com.hubdelivery.auth.presentation.dto.response.LoginResponse;
import com.hubdelivery.auth.presentation.dto.response.SignupResponse;
import com.hubdelivery.common.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

}
