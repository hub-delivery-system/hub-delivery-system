package com.hubdelivery.orderservice.order.domain.repository;

import com.hubdelivery.orderservice.order.domain.entity.Order;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID>, OrderRepositoryCustom {

    // 소프트 딜리트되지 않은 주문 단건 조회
    Optional<Order> findByIdAndDeletedAtIsNull(UUID id);
}
