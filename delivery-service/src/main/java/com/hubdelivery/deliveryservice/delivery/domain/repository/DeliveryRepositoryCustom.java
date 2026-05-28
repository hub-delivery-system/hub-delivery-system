package com.hubdelivery.deliveryservice.delivery.domain.repository;

import com.hubdelivery.deliveryservice.delivery.domain.entity.Delivery;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliverySearchCondition;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryRepositoryCustom {

    /**
     * 검색 조건에 따라 배송 목록을 조회합니다.
     *
     * @param cond         검색 조건 (status, startHubId, endHubId, orderId — 모두 선택적)
     * @param fixedHubId   HUB_MANAGER 권한일 때 강제 적용할 허브 ID (null이면 미적용)
     * @param fixedManagerId DELIVERY_MANAGER 권한일 때 강제 적용할 담당자 ID (null이면 미적용)
     * @param pageable     페이징 정보
     */
    Page<Delivery> searchDeliveries(
            DeliverySearchCondition cond,
            UUID fixedHubId,
            UUID fixedManagerId,
            Pageable pageable);
}
