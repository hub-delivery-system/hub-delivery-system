package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.application.dto.AiMessageRequestDto;
import com.hubdelivery.slack.application.dto.AiMessageResponseDto;
import com.hubdelivery.slack.application.dto.SlackMessageRequestDto;
import com.hubdelivery.slack.domain.entity.AiMessage;
import com.hubdelivery.slack.domain.exception.SlackErrorCode;
import com.hubdelivery.slack.domain.exception.SlackException;
import com.hubdelivery.slack.domain.repository.AiMessageRepository;
import com.hubdelivery.slack.infrastructure.client.OrderClient;
import com.hubdelivery.slack.infrastructure.client.dto.SlackNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiMessageService {

    private final AiMessageRepository aiMessageRepository;
    private final GeminiService geminiService;
    private final SlackService slackService;
    private final OrderClient orderClient;

    // 주문 생성 시 호출 — AI 최종 발송 시한 생성 + 허브 담당자 슬랙 알림
    public AiMessageResponseDto processOrderNotification(UUID orderId) {

        SlackNotificationDto orderInfo = orderClient.getSlackNotificationInfo(orderId);

        String aiResponse;
        String status;

        try {
            aiResponse = geminiService.generateDeadlineMessage(buildPrompt(orderInfo));
            status = "SUCCESS";
        } catch (Exception e) {
            log.error("AI API 호출 실패 - orderId: {}, error: {}", orderId, e.getMessage());
            aiResponse = "AI 응답 생성 실패";
            status = "FAIL";
        }

        AiMessage aiMessage = aiMessageRepository.save(
                AiMessage.builder()
                        .responseMessage(aiResponse)
                        .orderId(orderId)
                        .status(status)
                        .build()
        );

        // AI 성공 + 허브 담당자 슬랙 ID 있을 때만 전송
        if ("SUCCESS".equals(status) && orderInfo.getHubManagerSlackId() != null) {
            slackService.sendSlackMessage(
                    SlackMessageRequestDto.ofSystem(
                            orderInfo.getHubManagerSlackId(),
                            buildSlackMessage(orderInfo, aiResponse)
                    )
            );
        }

        return new AiMessageResponseDto(aiMessage);
    }

    // AI 응답 수동 저장 — POST /api/v1/ai-messages
    public AiMessageResponseDto saveAiMessage(AiMessageRequestDto requestDto) {
        AiMessage aiMessage = aiMessageRepository.save(
                AiMessage.builder()
                        .responseMessage(requestDto.getResponseMessage())
                        .orderId(requestDto.getOrderId())
                        .status(requestDto.getStatus())
                        .build()
        );
        return new AiMessageResponseDto(aiMessage);
    }

    @Transactional(readOnly = true)
    public AiMessageResponseDto getAiMessage(UUID id) {
        return new AiMessageResponseDto(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<AiMessageResponseDto> getAiMessages(Pageable pageable) {
        return aiMessageRepository.findAll(pageable).map(AiMessageResponseDto::new);
    }

    public void deleteAiMessage(UUID id, String deletedBy) {
        findById(id).softDelete(deletedBy);
    }

    private AiMessage findById(UUID id) {
        return aiMessageRepository.findById(id)
                .orElseThrow(() -> new SlackException(SlackErrorCode.AI_MESSAGE_NOT_FOUND));
    }

    // SA 명세 기준 Gemini 프롬프트 구성
    private String buildPrompt(SlackNotificationDto info) {
        return String.format("""
                아래 주문 정보를 바탕으로 최종 발송 시한을 알려주세요.
                배송 담당자 근무 시간: 09:00 ~ 18:00
                
                주문 번호: %s
                상품 및 요청 사항: %s
                현재 상태: %s
                발송지: %s
                경유지 및 도착지: %s
                
                응답 예시: "12월 10일 오전 9시까지 발송해야 납기를 맞출 수 있습니다."
                """,
                info.getOrderId(), info.getMessage(),
                info.getShippingStatus(), info.getHubName(), info.getRoute()
        );
    }

    // SA 명세 슬랙 알림 메시지 포맷
    private String buildSlackMessage(SlackNotificationDto info, String aiDeadline) {
        return String.format("""
                주문 번호 : %s
                허브 담당자 : %s
                발송지 : %s
                경유지 및 도착지 : %s
                현재 상태 : %s
                
                위 내용을 기반으로 도출된 최종 발송 시한은 %s
                """,
                info.getOrderId(), info.getHubManagerName(),
                info.getHubName(), info.getRoute(),
                info.getShippingStatus(), aiDeadline
        );
    }
}