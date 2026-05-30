package com.hubdelivery.slack.application.service;

/**
 * 이 파일은 예시입니다.
 *
 * 기존 AiMessageService를 통째로 교체하지 말고,
 * 아래 구조만 참고해서 현재 프로젝트의 AiMessageService에 필요한 부분을 이식하세요.
 */
public class AiMessageServiceIntegrationExample {

    /*
    private final DeadlineReasoningService deadlineReasoningService;
    private final GeminiService geminiService;
    private final SlackMessageService slackMessageService;

    public void processOrderNotification(UUID orderId) {
        SlackNotificationDto info = orderClient.getSlackNotificationInfo(orderId);

        DeadlineReasoningResult reasoning = deadlineReasoningService.calculate(
                LocalDateTime.now(),
                info.getDueAt(),
                info.getEstimatedDistanceKm(),
                info.getEstimatedDurationMinutes(),
                info.getWaypointCount()
        );

        String prompt = buildPrompt(info, reasoning);

        String aiMessage;
        try {
            aiMessage = geminiService.generateDeadlineMessage(prompt);
        } catch (Exception e) {
            // Gemini 장애 시에도 서버 계산 결과로 기본 메시지 제공
            aiMessage = reasoning.toFallbackMessage();
        }

        slackMessageService.sendMessage(info.getSlackId(), aiMessage);
    }

    private String buildPrompt(SlackNotificationDto info, DeadlineReasoningResult reasoning) {
        return String.format(\"\"\"
                당신은 물류 배송 알림 문구를 작성하는 AI입니다.
                아래 서버 계산 결과만 근거로 사용하세요.
                없는 정책이나 숫자를 추가로 만들지 마세요.

                [주문 정보]
                주문 ID: %s
                요청 사항: %s
                배송 경로: %s

                [서버 계산 결과]
                권장 발송 시각: %s
                예상 소요 시간: %d분
                적용 버퍼 시간: %d분
                장거리 배송 여부: %s
                경유 허브 수: %d

                [계산 근거]
                %s

                [응답 규칙]
                - 권장 발송 시각을 포함하세요.
                - 계산 근거를 한 문장으로 요약하세요.
                - 서버 계산 결과에 없는 숫자를 새로 만들지 마세요.
                \"\"\",
                info.getOrderId(),
                info.getRequestMessage(),
                info.getRoute(),
                reasoning.recommendedSendAt(),
                reasoning.estimatedDurationMinutes(),
                reasoning.bufferMinutes(),
                reasoning.longDistance() ? "예" : "아니오",
                reasoning.waypointCount(),
                String.join("\\n", reasoning.reasons())
        );
    }
    */
}
