package com.hubdelivery.slack.infrastructure.persistence;

import com.hubdelivery.slack.domain.entity.AiMessage;
import com.hubdelivery.slack.domain.repository.AiMessageRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaAiMessageRepository
        extends JpaRepository<AiMessage, UUID>, AiMessageRepository {
}