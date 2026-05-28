package com.hubdelivery.orderservice.order.presentation.dto;

import com.hubdelivery.orderservice.order.domain.type.OrderStatus;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderSearchCondition {

    private UUID producerId;     // 요청업체 ID 필터
    private UUID receiverId;     // 수령업체 ID 필터
    private UUID productId;      // 상품 ID 필터
    private UUID hubId;          // 허브 ID 필터 (MASTER 전용)
    private OrderStatus status;  // 주문 상태 필터
}
