package com.hubdelivery.orderservice.order.domain.type;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {

    PENDING,    // 주문 접수 대기
    CONFIRMED,  // 주문 확정
    SHIPPING,   // 배송 중
    COMPLETED,  // 배송 완료
    CANCELED;   // 주문 취소

    private Set<OrderStatus> allowedNext;

    // 허용된 상태 전이 규칙 정의
    static {
        PENDING.allowedNext   = EnumSet.of(CONFIRMED, CANCELED);
        CONFIRMED.allowedNext = EnumSet.of(SHIPPING, CANCELED);
        SHIPPING.allowedNext  = EnumSet.of(COMPLETED);
        COMPLETED.allowedNext = EnumSet.noneOf(OrderStatus.class); // 완료 후 전이 불가
        CANCELED.allowedNext  = EnumSet.noneOf(OrderStatus.class); // 취소 후 전이 불가
    }

    /** 현재 상태에서 next 상태로의 전이가 허용되는지 반환합니다. */
    public boolean canTransitionTo(OrderStatus next) {
        return allowedNext.contains(next);
    }
}
