package com.hubdelivery.deliveryservice.delivery.domain.type;

import java.util.EnumSet;
import java.util.Set;

public enum DeliveryStatus {

    HUB_PENDING,
    HUB_IN_TRANSIT,
    HUB_ARRIVED,
    DELIVERING,
    PARTNER_TRANSFER,
    DELIVERED;

    private Set<DeliveryStatus> allowedNext;

    static {
        HUB_PENDING.allowedNext      = EnumSet.of(HUB_IN_TRANSIT);
        HUB_IN_TRANSIT.allowedNext   = EnumSet.of(HUB_ARRIVED);
        HUB_ARRIVED.allowedNext      = EnumSet.of(DELIVERING);
        DELIVERING.allowedNext       = EnumSet.of(PARTNER_TRANSFER, DELIVERED);
        PARTNER_TRANSFER.allowedNext = EnumSet.of(DELIVERED);
        DELIVERED.allowedNext        = EnumSet.noneOf(DeliveryStatus.class);
    }

    /** 현재 상태에서 {@code next} 상태로의 전이가 허용되는지 반환합니다. */
    public boolean canTransitionTo(DeliveryStatus next) {
        return allowedNext.contains(next);
    }
}
