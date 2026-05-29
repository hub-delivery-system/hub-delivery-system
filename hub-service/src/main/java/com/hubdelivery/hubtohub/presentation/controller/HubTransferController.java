package com.hubdelivery.hubtohub.presentation.controller;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.hub.domain.type.UserRole;
import com.hubdelivery.hubtohub.application.dto.ResGetHubTransferDto;
import com.hubdelivery.hubtohub.application.service.HubTransferService;
import com.hubdelivery.hubtohub.presentation.dto.ReqCreateHubTransferDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


/**
 * Hub 이동 정보 (경로) 관리 API
 *
 * 책임: HTTP 요청 처리 및 응답 변환
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class HubTransferController {

    private final HubTransferService hubTransferService;

    /**
     * 새로운 Hub 이동 경로 생성
     * - 출발 허브와 도착 허브를 입력받아 경로 생성
     * - 이미 존재하면 HubToHubDuplicateLocationException 발생
     *
     * @param request 경로 생성 요청 (출발 허브 ID, 도착 허브 ID)
     * @return 생성된 경로 정보
     */
    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<ResGetHubTransferDto> createHubTransfer(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @Valid @RequestBody ReqCreateHubTransferDto request) {
        log.info("Hub 이동 경로 생성 요청 - from: {}, to: {}", request.getFromHubId(), request.getToHubId());

        ResGetHubTransferDto responseDto = hubTransferService.createHubTransfer(
                userId,
                request.getFromHubId(),
                request.getToHubId()
        );

        return ApiResponse.created(responseDto);
    }

    /**
     * 두 허브 간 경로 조회
     * - 캐시 → DB 순서로 조회
     * - 경로가 없으면 HubToHubNotFoundException 발생
     *
     * @param fromHubId 출발 허브 ID
     * @param toHubId 도착 허브 ID
     * @return 경로 정보 (경유지 포함)
     */
    @GetMapping
    @PreAuthorize("permitAll()")
    public ApiResponse<PageResponse<ResGetHubTransferDto>> getHubTransferList(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @RequestParam(name = "fromHubId", required = false) UUID fromHubId,
            @RequestParam(name = "toHubId", required = false) UUID toHubId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {

        log.info("Hub 이동 경로 목록 조회 - fromHubId: {}, toHubId: {}, page: {}, size: {}",
                fromHubId, toHubId, page, size);

        PageResponse<ResGetHubTransferDto> result = hubTransferService.getHubTransferList(
                fromHubId, toHubId, page, size, sort, userId
        );

        return ApiResponse.ok(result);
    }


    @GetMapping("/{transfer_id}")
    @PreAuthorize("permitAll()")
    public ApiResponse<ResGetHubTransferDto> getHubRouteByHubToHubId(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @PathVariable(name="transfer_id") UUID transferId
    ){
        return ApiResponse.ok(hubTransferService.getHubRouteByTransferId(transferId,userId));
    }



    /**
     * Hub 이동 경로 수정
     * - 경로 ID와 새로운 정보를 받아 수정
     *
     * @param transferId 경로 ID
     * @return 수정된 경로 정보
     */
    @PutMapping("/{transfer_id}")
    @PreAuthorize("hasRole('MASTER')")
    public ApiResponse<ResGetHubTransferDto> updateHubTransfer(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @PathVariable(name = "transfer_id") UUID transferId) {
        log.info("Hub 이동 경로 수정 - routeId: {}", transferId);

         ResGetHubTransferDto responseDto = hubTransferService.updateHubRouteByTransferId(transferId,userId);

        return ApiResponse.ok(responseDto);
    }

    /**
     * Hub 이동 경로 삭제
     * - 경로 ID로 soft delete 처리
     *
     * @path transfer_id 경로 ID
     * @return 성공 메시지
     */
    @DeleteMapping("/{transfer_id}")
    @PreAuthorize("hasRole('MASTER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHubTransfer(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @PathVariable(name = "transfer_id") UUID transferId) {
        log.info("Hub 이동 경로 삭제 - routeId: {}", transferId);
        hubTransferService.deleteHubRouteByTransferId(userId, transferId);
    }
}
