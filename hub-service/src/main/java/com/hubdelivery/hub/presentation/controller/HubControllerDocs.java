package com.hubdelivery.hub.presentation.controller;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.hub.application.dto.ResGetHubDto;
import com.hubdelivery.hub.domain.type.UserRole;
import com.hubdelivery.hub.presentation.dto.ReqHubDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Tag(name = "Hub", description = "허브 관리 API")
public interface HubControllerDocs {

    @Operation(summary = "허브 생성", description = "새로운 허브를 생성합니다. MASTER 권한이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "허브 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (MASTER만 가능)")
    })
    ApiResponse<ResGetHubDto> createHub(
            UUID userId,
            UserRole userRole,
            ReqHubDto request
    );

    @Operation(summary = "허브 단건 조회", description = "허브 ID로 특정 허브 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "허브 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음")
    })
    ApiResponse<ResGetHubDto> getHub(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "허브 ID") UUID hubId
    );

    @Operation(summary = "허브 목록 조회", description = "허브 목록을 페이지네이션으로 조회합니다. 키워드로 검색 가능합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "허브 목록 조회 성공")
    })
    ApiResponse<PageResponse<ResGetHubDto>> getHubs(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "검색 키워드 (허브명)") String keyword,
            Pageable pageable
    );

    @Operation(summary = "허브 수정", description = "허브 정보를 수정합니다. MASTER 권한이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "허브 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (MASTER만 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음")
    })
    ApiResponse<ResGetHubDto> updateHub(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "허브 ID") UUID hubId,
            ReqHubDto request
    );

    @Operation(summary = "허브 삭제", description = "허브를 삭제(Soft Delete)합니다. MASTER 권한이 필요합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "허브 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (MASTER만 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음")
    })
    ApiResponse<ResGetHubDto> deleteHub(
            UUID userId,
            UserRole userRole,
            @Parameter(description = "허브 ID") UUID hubId
    );
}
