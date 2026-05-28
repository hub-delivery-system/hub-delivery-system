package com.hubdelivery.orderservice.order.domain.repository;

import com.hubdelivery.orderservice.order.domain.entity.Order;
import com.hubdelivery.orderservice.order.presentation.dto.OrderSearchCondition;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {

    /**
     * 검색 조건과 권한 기반 필터를 적용하여 주문 목록을 조회합니다.
     *
     * @param cond            검색 조건 (producerId, receiverId, productId, status — 모두 선택적)
     * @param fixedProducerId DELIVERY_MANAGER·COMPANY_MANAGER 권한일 때 강제 적용할 요청자 ID (null이면 미적용)
     * @param pageable        페이징 정보
     */
    Page<Order> searchOrders(
            OrderSearchCondition cond,
            UUID fixedProducerId,
            Pageable pageable);
}
