package com.hubdelivery.company.company.presentation.controller;

import java.util.UUID;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.company.presentation.dto.request.CompanyCreateRequestDto;
import com.hubdelivery.company.company.presentation.dto.request.CompanyUpdateRequestDto;
import com.hubdelivery.company.company.presentation.dto.response.CompanyResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Company", description = "업체 API")
public interface CompanyControllerDocs {

    @Operation(summary = "업체 등록", description = "업체 정보를 등록합니다. MASTER 또는 담당 허브의 HUB_MANAGER 권한이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "업체 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ApiResponse<CompanyResponseDto> createCompany(
            UUID userId,
            UserRole userRole,
            CompanyCreateRequestDto request
    );

    @Operation(summary = "업체 조회 및 검색", description = "업체 목록을 검색 조건과 페이지 조건으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    ApiResponse<PageResponse<CompanyResponseDto>> getAllCompanies(
            @Parameter(description = "업체명/주소 검색어")
            String keyword,
            @Parameter(description = "페이지 번호")
            Integer page,
            @Parameter(description = "페이지 크기")
            Integer size,
            @Parameter(description = "정렬 조건")
            String sort
    );

    @Operation(summary = "업체 상세 조회", description = "업체 ID로 업체 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "업체를 찾을 수 없음")
    })
    ApiResponse<CompanyResponseDto> getCompany(
            @Parameter(description = "업체 ID")
            UUID companyId
    );

    @Operation(summary = "업체 수정", description = "업체 정보를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "업체를 찾을 수 없음")
    })
    ApiResponse<CompanyResponseDto> updateCompany(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "업체 ID")
            UUID companyId,
            CompanyUpdateRequestDto request
    );

    @Operation(summary = "업체 삭제", description = "업체를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "업체를 찾을 수 없음")
    })
    ApiResponse<Void> deleteCompany(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "업체 ID")
            UUID companyId
    );
}
