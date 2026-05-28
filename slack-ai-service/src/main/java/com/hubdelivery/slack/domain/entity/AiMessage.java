package com.hubdelivery.slack.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_ai_messages")
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class AiMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "response_message", nullable = false, columnDefinition = "TEXT")
    private String responseMessage;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Builder
    public AiMessage(String responseMessage, UUID orderId, String status) {
        this.responseMessage = responseMessage;
        this.orderId         = orderId;
        this.status          = status;
    }
}