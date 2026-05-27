package com.hubdelivery.deliveryservice.deliverymanager.presentation.dto;

import com.hubdelivery.deliveryservice.deliverymanager.domain.entity.DeliveryManager;
import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DeliveryManagerResponse {

    private final UUID id;
    private final UUID userId;
    private final UUID hubId;
    private final UUID companyId;
    private final DeliveryManagerType type;
    private final Integer sequence;
    private final LocalDateTime createdAt;

    private DeliveryManagerResponse(DeliveryManager manager) {
        this.id = manager.getId();
        this.userId = manager.getUserId();
        this.hubId = manager.getHubId();
        this.companyId = manager.getCompanyId();
        this.type = manager.getType();
        this.sequence = manager.getSequence();
        this.createdAt = manager.getCreatedAt();
    }

    public static DeliveryManagerResponse from(DeliveryManager manager) {
        return new DeliveryManagerResponse(manager);
    }
}
