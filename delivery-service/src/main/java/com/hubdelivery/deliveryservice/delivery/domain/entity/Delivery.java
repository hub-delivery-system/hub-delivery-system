package com.hubdelivery.deliveryservice.delivery.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.deliveryservice.delivery.domain.type.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_deliveries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false)
    private UUID startHubId;

    @Column(nullable = false)
    private UUID endHubId;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String slackId;

    private UUID deliveryManagerId;

    @Builder
    private Delivery(UUID orderId, DeliveryStatus status, UUID startHubId, UUID endHubId,
                     String address, UUID userId, String slackId, UUID deliveryManagerId) {
        this.orderId = orderId;
        this.status = status;
        this.startHubId = startHubId;
        this.endHubId = endHubId;
        this.address = address;
        this.userId = userId;
        this.slackId = slackId;
        this.deliveryManagerId = deliveryManagerId;
    }

    public void update(DeliveryStatus status, String address, UUID userId, String slackId, UUID deliveryManagerId) {
        this.status = status;
        this.address = address;
        this.userId = userId;
        this.slackId = slackId;
        this.deliveryManagerId = deliveryManagerId;
    }
}
