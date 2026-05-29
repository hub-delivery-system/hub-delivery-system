package com.hubdelivery.deliveryservice.delivery.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubdelivery.common.event.OrderCreatedEvent;
import com.hubdelivery.deliveryservice.delivery.application.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.created", groupId = "delivery-service")
    public void consume(String payload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.debug("order.created 이벤트 수신: orderId={}", event.getOrderId());
            deliveryService.createFromEvent(event);
        } catch (Exception e) {
            log.error("order.created 이벤트 처리 실패: payload={}", payload, e);
        }
    }
}
