package com.hubdelivery.company.company.presentation.controller;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.company.company.application.service.CompanyService;
import com.hubdelivery.company.company.presentation.dto.request.CompanyCreateRequestDto;
import com.hubdelivery.company.company.presentation.dto.response.CompanyCreateResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Company", description = "업체 API")
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @Operation(summary = "업체 등록", description = "업체 정보를 등록합니다. MASTER 또는 담당 허브의 HUB_MANAGER 권한이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "업체 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CompanyCreateResponseDto> createCompany(@Valid @RequestBody CompanyCreateRequestDto request) {
        return ApiResponse.created(companyService.createCompany(request));
    }
}
