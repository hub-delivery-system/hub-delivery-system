package com.hubdelivery.company.company.presentation.controller;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.company.application.service.CompanyService;
import com.hubdelivery.company.company.presentation.dto.request.CompanyCreateRequestDto;
import com.hubdelivery.company.company.presentation.dto.request.CompanyUpdateRequestDto;
import com.hubdelivery.company.company.presentation.dto.response.CompanyResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController implements CompanyControllerDocs {

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_ROLE = "X-Role";

    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<CompanyResponseDto> createCompany(
            @RequestHeader(X_USER_ID) UUID userId,
            @RequestHeader(X_ROLE) UserRole userRole,
            @Valid @RequestBody CompanyCreateRequestDto request
    ) {
        return ApiResponse.created(companyService.createCompany(userId, userRole, request));
    }

    @GetMapping
    @Override
    public ApiResponse<PageResponse<CompanyResponseDto>> getAllCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(companyService.getAllCompanies(keyword, page, size, sort));
    }

    @GetMapping("/{companyId}")
    @Override
    public ApiResponse<CompanyResponseDto> getCompany(
            @PathVariable UUID companyId
    ) {
        return ApiResponse.ok(companyService.getCompany(companyId));
    }

    @PutMapping("/{companyId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_MANAGER')")
    @Override
    public ApiResponse<CompanyResponseDto> updateCompany(
            @RequestHeader(X_USER_ID) UUID userId,
            @RequestHeader(X_ROLE) UserRole userRole,
            @PathVariable UUID companyId,
            @Valid @RequestBody CompanyUpdateRequestDto request
    ) {
        return ApiResponse.ok(companyService.updateCompany(userId, userRole, companyId, request));
    }

    @DeleteMapping("/{companyId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    @Override
    public ApiResponse<Void> deleteCompany(
            @RequestHeader(X_USER_ID) UUID userId,
            @RequestHeader(X_ROLE) UserRole userRole,
            @PathVariable UUID companyId
    ) {
        companyService.deleteCompany(userId, userRole, companyId);
        return ApiResponse.ok(null);
    }
}
