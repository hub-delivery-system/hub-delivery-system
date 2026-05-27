package com.hubdelivery.deliveryservice.deliverymanager.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
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
@Table(name = "p_delivery_managers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryManager extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID hubId;

    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryManagerType type;

    @Column(nullable = false)
    private Integer sequence;

    @Builder
    private DeliveryManager(UUID userId, UUID hubId, UUID companyId,
                            DeliveryManagerType type, Integer sequence) {
        this.userId = userId;
        this.hubId = hubId;
        this.companyId = companyId;
        this.type = type;
        this.sequence = sequence;
    }

    public void update(UUID hubId, UUID companyId, DeliveryManagerType type, Integer sequence) {
        this.hubId = hubId;
        this.companyId = companyId;
        this.type = type;
        this.sequence = sequence;
    }
}
