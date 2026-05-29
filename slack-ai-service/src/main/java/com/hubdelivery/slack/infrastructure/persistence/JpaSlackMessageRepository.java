package com.hubdelivery.slack.infrastructure.persistence;

import com.hubdelivery.slack.domain.entity.SlackMessage;
import com.hubdelivery.slack.domain.repository.SlackMessageRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaSlackMessageRepository
        extends JpaRepository<SlackMessage, UUID>, SlackMessageRepository, SlackQueryDslRepository {
}