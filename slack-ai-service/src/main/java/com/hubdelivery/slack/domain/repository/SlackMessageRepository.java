package com.hubdelivery.slack.domain.repository;

import com.hubdelivery.slack.application.dto.SlackMessageResponseDto;
import com.hubdelivery.slack.application.dto.SlackSearchRequestDto;
import com.hubdelivery.slack.domain.entity.SlackMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SlackMessageRepository {
    SlackMessage save(SlackMessage slackMessage);
    Optional<SlackMessage> findById(UUID id);
    Page<SlackMessageResponseDto> search(SlackSearchRequestDto requestDto, Pageable pageable);
}