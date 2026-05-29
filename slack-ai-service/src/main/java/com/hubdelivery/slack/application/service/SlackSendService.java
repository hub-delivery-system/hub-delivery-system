package com.hubdelivery.slack.application.service;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
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
            return false;
        }
    }
}