package com.hubdelivery.deliveryservice.deliveryroute.domain.type;

public enum DeliveryRouteStatus {

    WAITING_AT_HUB,
    IN_TRANSIT,
    ARRIVED_AT_HUB,
    OUT_FOR_DELIVERY,
    DELIVERED;

    /** 선형 전이만 허용: 현재 ordinal + 1 로의 이동만 가능합니다. */
    public boolean canTransitionTo(DeliveryRouteStatus next) {
        return next.ordinal() == this.ordinal() + 1;
    }
}
