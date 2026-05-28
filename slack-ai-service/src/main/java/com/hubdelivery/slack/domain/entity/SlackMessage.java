package com.hubdelivery.slack.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.slack.domain.type.SendStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_slack_messages")
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class SlackMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    // 시스템 발송 시 null 허용 (AI 알림 등 내부 발송)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "slack_id", nullable = false, length = 50)
    private String slackId;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SendStatus status;

    @Builder
    public SlackMessage(UUID userId, String slackId, String message) {
        this.userId  = userId;
        this.slackId = slackId;
        this.message = message;
        this.status  = SendStatus.PENDING;
    }

    public void markSuccess() {
        this.status = SendStatus.SUCCESS;
        this.sentAt = LocalDateTime.now();
    }

    public void markFail() {
        this.status = SendStatus.FAIL;
    }

    public void updateMessage(String message) {
        this.message = message;
    }
}