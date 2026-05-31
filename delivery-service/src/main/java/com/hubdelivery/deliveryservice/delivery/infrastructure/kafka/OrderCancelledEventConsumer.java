package com.hubdelivery.deliveryservice.delivery.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubdelivery.common.event.OrderCancelledEvent;
import com.hubdelivery.deliveryservice.delivery.application.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledEventConsumer {

    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.cancelled", groupId = "delivery-service")
    public void consume(String payload) {
        try {
            OrderCancelledEvent event = objectMapper.readValue(payload, OrderCancelledEvent.class);
            log.debug("order.cancelled 이벤트 수신: orderId={}", event.getOrderId());
            deliveryService.cancelByOrderId(event.getOrderId());
        } catch (Exception e) {
            log.error("order.cancelled 이벤트 처리 실패: payload={}", payload, e);
        }
    }
}
