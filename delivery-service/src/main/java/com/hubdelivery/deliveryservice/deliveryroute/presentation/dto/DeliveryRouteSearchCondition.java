package com.hubdelivery.deliveryservice.deliveryroute.presentation.dto;

import com.hubdelivery.deliveryservice.deliveryroute.domain.type.DeliveryRouteStatus;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryRouteSearchCondition {

    /** 경로 상태 필터 (선택) */
    private DeliveryRouteStatus status;

    /** 출발 허브 ID 필터 (선택) */
    private UUID startHubId;

    /** 도착 허브 ID 필터 (선택) */
    private UUID endHubId;

    /** 담당자 ID 필터 (선택) */
    private UUID deliveryManagerId;
}
