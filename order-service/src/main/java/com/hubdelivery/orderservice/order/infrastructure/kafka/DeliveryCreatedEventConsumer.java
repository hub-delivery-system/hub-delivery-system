package com.hubdelivery.orderservice.order.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubdelivery.common.event.DeliveryCreatedEvent;
import com.hubdelivery.orderservice.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryCreatedEventConsumer {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "delivery.created", groupId = "order-service")
    @Transactional
    public void consume(String payload) {
        try {
            DeliveryCreatedEvent event = objectMapper.readValue(payload, DeliveryCreatedEvent.class);
            orderRepository.findByIdAndDeletedAtIsNull(event.getOrderId())
                    .ifPresent(order -> {
                        order.assignDelivery(event.getDeliveryId());
                        log.debug("deliveryId 연결 완료: orderId={}, deliveryId={}", event.getOrderId(), event.getDeliveryId());
                    });
        } catch (Exception e) {
            log.error("delivery.created 이벤트 처리 실패: payload={}", payload, e);
        }
    }
}
