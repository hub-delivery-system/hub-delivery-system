package com.hubdelivery.deliveryservice.delivery.presentation.dto;

import com.hubdelivery.deliveryservice.delivery.domain.type.DeliveryStatus;
import java.time.LocalDate;
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

    /**
     * 배송 담당자 ID 필터 (선택).
     * MASTER 권한에서만 유효하며, DELIVERY_MANAGER 권한은 X-User-Id 헤더로 자동 고정되므로 이 값을 무시한다.
     * 주 용도: slack-service 스케줄러가 Feign으로 특정 담당자의 배송 목록을 조회할 때 사용.
     */
    private UUID deliveryManagerId;

    /**
     * 생성일 기준 날짜 필터 (선택).
     * 지정 시 해당 날짜 00:00:00 이상 ~ 익일 00:00:00 미만 범위로 조회한다.
     * 주 용도: 스케줄러에서 당일 배송 목록만 추출할 때 사용.
     */
    private LocalDate date;
}
