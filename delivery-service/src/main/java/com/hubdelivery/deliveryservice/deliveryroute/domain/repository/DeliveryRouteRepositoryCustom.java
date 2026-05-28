package com.hubdelivery.deliveryservice.deliveryroute.domain.repository;

import com.hubdelivery.deliveryservice.deliveryroute.domain.entity.DeliveryRoute;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteSearchCondition;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryRouteRepositoryCustom {

    /**
     * 배송 ID + 검색 조건에 따라 경로 목록을 조회합니다.
     *
     * @param deliveryId     대상 배송 ID
     * @param cond           검색 조건 (status, startHubId, endHubId, deliveryManagerId — 모두 선택적)
     * @param fixedHubId     HUB_MANAGER 권한일 때 강제 적용할 허브 ID (null이면 미적용)
     * @param fixedManagerId DELIVERY_MANAGER 권한일 때 강제 적용할 담당자 ID (null이면 미적용)
     * @param pageable       페이징 정보
     */
    Page<DeliveryRoute> searchRoutes(
            UUID deliveryId,
            DeliveryRouteSearchCondition cond,
            UUID fixedHubId,
            UUID fixedManagerId,
            Pageable pageable);
}
