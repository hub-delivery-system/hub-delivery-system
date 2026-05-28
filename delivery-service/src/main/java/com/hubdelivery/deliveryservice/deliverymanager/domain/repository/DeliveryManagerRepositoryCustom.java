package com.hubdelivery.deliveryservice.deliverymanager.domain.repository;

import com.hubdelivery.deliveryservice.deliverymanager.domain.entity.DeliveryManager;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerSearchCondition;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryManagerRepositoryCustom {

    /**
     * 검색 조건에 따라 배송 담당자 목록을 조회합니다.
     * HUB_MANAGER인 경우 hubId로 사전 필터링된 상태로 호출됩니다.
     *
     * @param cond    검색 조건 (type, hubId, userId — 모두 선택적)
     * @param fixedHubId HUB_MANAGER 권한일 때 강제 적용할 허브 ID (null이면 미적용)
     * @param pageable 페이징 정보
     */
    Page<DeliveryManager> searchManagers(
            DeliveryManagerSearchCondition cond,
            UUID fixedHubId,
            Pageable pageable);
}
