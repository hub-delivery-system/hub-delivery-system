package com.hubdelivery.slack.application.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI에게 넘기기 전, 서버가 먼저 계산한 발송 시한 추론 결과.
 *
 * 목적:
 * - Gemini가 임의로 시간을 추측하지 않도록 서버 계산 결과를 제공한다.
 * - Gemini 장애 시 fallback 메시지 생성에도 사용할 수 있다.
 */
public record DeadlineReasoningResult(
        LocalDateTime recommendedSendAt,
        long estimatedDurationMinutes,
        long bufferMinutes,
        boolean longDistance,
        int waypointCount,
        List<String> reasons
) {

    public String toFallbackMessage() {
        return String.format(
                "최종 발송 권장 시각은 %s입니다. 근거: %s",
                recommendedSendAt,
                String.join(" / ", reasons)
        );
    }
}
