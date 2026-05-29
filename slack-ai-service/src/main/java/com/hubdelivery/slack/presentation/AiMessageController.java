package com.hubdelivery.slack.presentation;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.slack.application.dto.AiMessageRequestDto;
import com.hubdelivery.slack.application.dto.AiMessageResponseDto;
import com.hubdelivery.slack.application.service.AiMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai-messages")
@RequiredArgsConstructor
@Tag(name = "AI Message", description = "AI 메시지 API")
public class AiMessageController {

    private final AiMessageService aiMessageService;

    @Operation(summary = "AI 응답 저장")
    @PostMapping
    public ResponseEntity<ApiResponse<AiMessageResponseDto>> saveAiMessage(
            @Valid @RequestBody AiMessageRequestDto requestDto) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created(aiMessageService.saveAiMessage(requestDto)));
    }

    // order-service에서 주문 생성 시 Feign으로 호출하는 내부 API
    @Operation(summary = "주문 기반 AI 발송 시한 분석 + 슬랙 알림")
    @PostMapping("/order/{orderId}/notify")
    public ResponseEntity<ApiResponse<AiMessageResponseDto>> processOrderNotification(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(
                aiMessageService.processOrderNotification(orderId)));
    }

    @Operation(summary = "AI 응답 목록 조회")
    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<AiMessageResponseDto>>> getAiMessages(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(aiMessageService.getAiMessages(pageable)));
    }

    @Operation(summary = "AI 응답 상세 조회")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<AiMessageResponseDto>> getAiMessage(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(aiMessageService.getAiMessage(id)));
    }

    @Operation(summary = "AI 응답 삭제")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> deleteAiMessage(
            @PathVariable UUID id,
            @RequestHeader("X-User-Name") String username) {
        aiMessageService.deleteAiMessage(id, username);
        return ResponseEntity.noContent().build();
    }
}