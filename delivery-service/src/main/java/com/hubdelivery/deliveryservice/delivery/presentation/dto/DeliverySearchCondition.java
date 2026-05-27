package com.hubdelivery.deliveryservice.delivery.presentation.dto;

import com.hubdelivery.deliveryservice.delivery.domain.type.DeliveryStatus;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliverySearchCondition {

    /** 배송 상태 필터 (선택) */
    private DeliveryStatus status;

    /** 출발 허브 ID 필터 (선택) */
    private UUID startHubId;

    /** 도착 허브 ID 필터 (선택) */
    private UUID endHubId;

    /** 주문 ID 필터 (선택) */
    private UUID orderId;
}
