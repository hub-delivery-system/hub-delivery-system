package com.hubdelivery.hubtohub.presentation.controller;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.hub.domain.type.UserRole;
import com.hubdelivery.hubtohub.application.dto.ResGetHubTransferDto;
import com.hubdelivery.hubtohub.presentation.dto.ReqCreateHubTransferDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "HubTransfer", description = "허브 간 경로 관리 API")
public interface HubTransferControllerDocs {

    @Operation(summary = "허브 간 경로 생성", description = "출발 허브와 도착 허브를 입력받아 경로를 생성합니다. 이미 존재하는 경로면 예외가 발생합니다. MASTER 권한이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "경로 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (MASTER만 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 경로")
    })
    ApiResponse<ResGetHubTransferDto> createHubTransfer(
            UUID userId,
            UserRole userRole,
            ReqCreateHubTransferDto request
    );

    @Operation(summary = "허브 간 경로 목록 조회", description = "허브 간 경로 목록을 페이지네이션으로 조회합니다. 출발/도착 허브 ID로 필터링 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "경로 목록 조회 성공")
    })
    ApiResponse<PageResponse<ResGetHubTransferDto>> getHubTransferList(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "출발 허브 ID (선택)") UUID fromHubId,
            @Parameter(description = "도착 허브 ID (선택)") UUID toHubId,
            @Parameter(description = "페이지 번호 (0부터 시작)") int page,
            @Parameter(description = "페이지 크기 (기본값: 10)") int size,
            @Parameter(description = "정렬 기준 (예: createdAt,desc)") String sort
    );

    @Operation(summary = "허브 간 경로 단건 조회", description = "경로 ID로 특정 허브 간 경로를 조회합니다. 캐시 → DB 순서로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "경로 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "경로를 찾을 수 없음")
    })
    ApiResponse<ResGetHubTransferDto> getHubRouteByHubToHubId(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "경로 ID") UUID transferId
    );

    @Operation(summary = "허브 간 경로 수정", description = "경로 ID로 허브 간 경로를 재계산하여 수정합니다. 카카오 API를 통해 새로운 거리와 시간을 계산합니다. MASTER 권한이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "경로 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (MASTER만 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "경로를 찾을 수 없음")
    })
    ApiResponse<ResGetHubTransferDto> updateHubTransfer(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "경로 ID") UUID transferId
    );

    @Operation(summary = "허브 간 경로 삭제", description = "경로 ID로 허브 간 경로를 삭제(Soft Delete)합니다. 경유지도 함께 삭제되며, 캐시도 제거됩니다. MASTER 권한이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "경로 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (MASTER만 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "경로를 찾을 수 없음")
    })
    void deleteHubTransfer(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "경로 ID") UUID transferId
    );
}
