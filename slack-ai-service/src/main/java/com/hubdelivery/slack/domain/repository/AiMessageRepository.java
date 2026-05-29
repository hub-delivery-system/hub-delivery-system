package com.hubdelivery.slack.domain.repository;

import com.hubdelivery.slack.domain.entity.AiMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AiMessageRepository {
    AiMessage save(AiMessage aiMessage);
    Optional<AiMessage> findById(UUID id);
    Page<AiMessage> findAll(Pageable pageable);
}