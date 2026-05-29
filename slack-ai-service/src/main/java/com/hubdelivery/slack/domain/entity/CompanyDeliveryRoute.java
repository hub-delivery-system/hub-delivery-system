package com.hubdelivery.slack.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.slack.domain.type.CompanyDeliveryStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_company_delivery_routes")
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class CompanyDeliveryRoute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "start_hub_id")
    private UUID startHubId;

    @Column(name = "receiver_company_id")
    private UUID receiverCompanyId;

    @Column(name = "estimated_distance", precision = 10, scale = 2)
    private BigDecimal estimatedDistance;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @Column(name = "real_distance", precision = 10, scale = 2)
    private BigDecimal realDistance;

    @Column(name = "real_duration")
    private Integer realDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CompanyDeliveryStatus status;

    @Column(name = "delivery_manager_id")
    private UUID deliveryManagerId;

    @Column(name = "delivery_order")
    private Integer deliveryOrder;

    @Builder
    public CompanyDeliveryRoute(UUID deliveryId, UUID startHubId, UUID receiverCompanyId,
                                BigDecimal estimatedDistance, Integer estimatedDuration,
                                UUID deliveryManagerId, Integer deliveryOrder) {
        this.deliveryId        = deliveryId;
        this.startHubId        = startHubId;
        this.receiverCompanyId = receiverCompanyId;
        this.estimatedDistance = estimatedDistance;
        this.estimatedDuration = estimatedDuration;
        this.deliveryManagerId = deliveryManagerId;
        this.deliveryOrder     = deliveryOrder;
        this.status            = CompanyDeliveryStatus.COMPANY_IN_TRANSIT;
    }

    public void complete(BigDecimal realDistance, Integer realDuration) {
        this.realDistance = realDistance;
        this.realDuration = realDuration;
        this.status       = CompanyDeliveryStatus.DELIVERED;
    }
}
