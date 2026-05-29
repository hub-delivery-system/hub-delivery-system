package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.application.dto.SlackMessageRequestDto;
import com.hubdelivery.slack.application.dto.SlackMessageResponseDto;
import com.hubdelivery.slack.application.dto.SlackSearchRequestDto;
import com.hubdelivery.slack.domain.entity.SlackMessage;
import com.hubdelivery.slack.domain.exception.SlackErrorCode;
import com.hubdelivery.slack.domain.exception.SlackException;
import com.hubdelivery.slack.domain.repository.SlackMessageRepository;
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
public class SlackService {

    private final SlackMessageRepository slackMessageRepository;
    private final SlackSendService slackSendService;

    // POST /api/v1/slack-messages — 생성 + 즉시 전송
    public SlackMessageResponseDto sendSlackMessage(SlackMessageRequestDto requestDto) {

        SlackMessage slackMessage = slackMessageRepository.save(requestDto.toEntity());

        boolean success = slackSendService.sendMessage(
                slackMessage.getSlackId(),
                slackMessage.getMessage()
        );

        if (success) {
            slackMessage.markSuccess();
        } else {
            slackMessage.markFail();
            log.warn("슬랙 메시지 전송 실패 - id: {}, slackId: {}",
                    slackMessage.getId(), slackMessage.getSlackId());
        }

        return new SlackMessageResponseDto(slackMessage);
    }

    @Transactional(readOnly = true)
    public SlackMessageResponseDto getSlackMessage(UUID id) {
        return new SlackMessageResponseDto(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<SlackMessageResponseDto> searchSlackMessages(
            SlackSearchRequestDto requestDto, Pageable pageable) {
        return slackMessageRepository.search(requestDto, pageable);
    }

    public SlackMessageResponseDto updateSlackMessage(
            UUID id, SlackMessageRequestDto requestDto, String updatedBy) {
        SlackMessage slackMessage = findById(id);
        slackMessage.updateMessage(requestDto.getMessage());
        return new SlackMessageResponseDto(slackMessage);
    }

    public void deleteSlackMessage(UUID id, String deletedBy) {
        findById(id).softDelete(deletedBy);
    }

    private SlackMessage findById(UUID id) {
        return slackMessageRepository.findById(id)
                .orElseThrow(() -> new SlackException(SlackErrorCode.SLACK_MESSAGE_NOT_FOUND));
    }
}