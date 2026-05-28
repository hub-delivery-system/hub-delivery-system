package com.hubdelivery.orderservice.order.domain.repository;

import com.hubdelivery.orderservice.order.domain.entity.OutboxEvent;
import com.hubdelivery.orderservice.order.domain.type.OutboxEventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatus(OutboxEventStatus status);

    List<OutboxEvent> findByStatusIn(List<OutboxEventStatus> statuses);
}
