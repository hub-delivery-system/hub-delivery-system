package com.hubdelivery.user.presentation.controller;

import java.util.UUID;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.type.UserStatus;
import com.hubdelivery.user.presentation.dto.response.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User", description = "사용자 관리 API")
public interface UserControllerDocs {

    @Operation(summary = "사용자 목록 조회", description = "필터 조건 + 페이징/정렬로 사용자 목록을 조회합니다. MASTER 권한이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ApiResponse<PageResponse<UserResponse>> getUsers(
            @Parameter(description = "페이지 번호(0부터 시작), 기본값 0")
            Integer page,
            @Parameter(description = "페이지 크기(허용값: 10, 30, 50 / 그 외 입력 시 10으로 보정)")
            Integer size,
            @Parameter(description = "정렬(허용필드: createdAt, updatedAt / 예: createdAt,DESC)")
            String sort,
            @Parameter(description = "username 포함 검색")
            String username,
            @Parameter(description = "name 포함 검색(현재 username과 동일하게 동작)")
            String name,
            @Parameter(description = "역할 필터(MASTER, HUB_MANAGER, DELIVERY_MANAGER, COMPANY_MANAGER)")
            UserRole role,
            @Parameter(description = "상태 필터(PENDING, APPROVED, REJECTED)")
            UserStatus status,
            @Parameter(description = "허브 ID 필터")
            UUID hubId,
            @Parameter(description = "업체 ID 필터")
            UUID companyId
    );
}
