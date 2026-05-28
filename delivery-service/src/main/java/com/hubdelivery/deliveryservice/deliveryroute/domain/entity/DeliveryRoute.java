package com.hubdelivery.deliveryservice.deliveryroute.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.deliveryservice.deliveryroute.domain.type.DeliveryRouteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_delivery_routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID deliveryId;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false)
    private UUID startHubId;

    @Column(nullable = false)
    private UUID endHubId;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedDistance;

    private Integer estimatedDuration;

    @Column(precision = 10, scale = 2)
    private BigDecimal realDistance;

    private Integer realDuration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryRouteStatus status;

    private UUID deliveryManagerId;

    @Builder
    private DeliveryRoute(UUID deliveryId, Integer sequence, UUID startHubId, UUID endHubId,
                          BigDecimal estimatedDistance, Integer estimatedDuration,
                          DeliveryRouteStatus status, UUID deliveryManagerId) {
        this.deliveryId = deliveryId;
        this.sequence = sequence;
        this.startHubId = startHubId;
        this.endHubId = endHubId;
        this.estimatedDistance = estimatedDistance;
        this.estimatedDuration = estimatedDuration;
        this.status = status;
        this.deliveryManagerId = deliveryManagerId;
    }

    public void update(DeliveryRouteStatus status, BigDecimal realDistance, Integer realDuration, UUID deliveryManagerId) {
        this.status = status;
        this.realDistance = realDistance;
        this.realDuration = realDuration;
        this.deliveryManagerId = deliveryManagerId;
    }
}
