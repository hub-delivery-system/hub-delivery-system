package com.hubdelivery.slack.application.service;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class SlackSendService {

    @Value("${slack.bot.token}")
    private String botToken;

    private final Slack slack = Slack.getInstance();

    /**
     * Circuit Breaker: "slack"
     * - Slack API 장애 시 false 반환 → SlackService에서 markFail() 처리됨
     * - Circuit OPEN 중에는 Slack 호출 자체를 건너뜀
     */
    @CircuitBreaker(name = "slack", fallbackMethod = "sendMessageFallback")
    public boolean sendMessage(String slackId, String message) {
        try {
            MethodsClient methods = slack.methods(botToken);

            ChatPostMessageResponse response = methods.chatPostMessage(
                    ChatPostMessageRequest.builder()
                            .channel(slackId)
                            .text(message)
                            .build()
            );

            if (!response.isOk()) {
                log.error("슬랙 전송 실패 - slackId: {}, error: {}", slackId, response.getError());
                return false;
            }

            log.info("슬랙 전송 성공 - slackId: {}", slackId);
            return true;

        } catch (SlackApiException | IOException e) {
            log.error("슬랙 API 예외 - slackId: {}, error: {}", slackId, e.getMessage());
            //    checked exception은 CircuitBreaker가 기본으로 카운트 안 함
            //    application.yaml의 recordExceptions 설정으로 명시적으로 잡아줌
            throw new RuntimeException("Slack API 호출 실패", e);
        }
    }

    @SuppressWarnings("unused")
    private boolean sendMessageFallback(String slackId, String message, Throwable t) {
        log.warn("[Slack CircuitBreaker] fallback 실행 - slackId: {}, 원인: {}",
                slackId, t.getMessage());
        return false;
    }
}