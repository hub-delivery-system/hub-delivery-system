package com.hubdelivery.slack.infrastructure.persistence;

import com.hubdelivery.slack.application.dto.SlackMessageResponseDto;
import com.hubdelivery.slack.application.dto.SlackSearchRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SlackQueryDslRepository {
    Page<SlackMessageResponseDto> search(SlackSearchRequestDto requestDto, Pageable pageable);
}