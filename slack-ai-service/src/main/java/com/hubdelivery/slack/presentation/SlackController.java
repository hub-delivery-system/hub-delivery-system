package com.hubdelivery.slack.presentation;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.slack.application.dto.SlackMessageRequestDto;
import com.hubdelivery.slack.application.dto.SlackMessageResponseDto;
import com.hubdelivery.slack.application.dto.SlackSearchRequestDto;
import com.hubdelivery.slack.application.service.SlackService;
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
@RequestMapping("/api/v1/slack-messages")
@RequiredArgsConstructor
@Tag(name = "Slack Message", description = "슬랙 메시지 API")
public class SlackController {

    private final SlackService slackService;

    @Operation(summary = "슬랙 메시지 발송")
    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER','HUB_MANAGER','DELIVERY_MANAGER','COMPANY_MANAGER')")
    public ResponseEntity<ApiResponse<SlackMessageResponseDto>> sendSlackMessage(
            @Valid @RequestBody SlackMessageRequestDto requestDto) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created(slackService.sendSlackMessage(requestDto)));
    }

    @Operation(summary = "슬랙 메시지 상세 조회")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<SlackMessageResponseDto>> getSlackMessage(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(slackService.getSlackMessage(id)));
    }

    @Operation(summary = "슬랙 메시지 목록 조회 및 검색")
    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<SlackMessageResponseDto>>> searchSlackMessages(
            @ModelAttribute SlackSearchRequestDto requestDto,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                slackService.searchSlackMessages(requestDto, pageable)));
    }

    @Operation(summary = "슬랙 메시지 이력 수정")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ApiResponse<SlackMessageResponseDto>> updateSlackMessage(
            @PathVariable UUID id,
            @Valid @RequestBody SlackMessageRequestDto requestDto,
            @RequestHeader("X-User-Name") String username) {
        return ResponseEntity.ok(ApiResponse.ok(
                slackService.updateSlackMessage(id, requestDto, username)));
    }

    @Operation(summary = "슬랙 메시지 이력 삭제")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> deleteSlackMessage(
            @PathVariable UUID id,
            @RequestHeader("X-User-Name") String username) {
        slackService.deleteSlackMessage(id, username);
        return ResponseEntity.noContent().build();
    }
}