package com.hubdelivery.deliveryservice.delivery.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    // 처리 실패 시 예외를 던져 KafkaConsumerConfig의 재시도·DLT 처리에 맡긴다
    @KafkaListener(topics = "order.cancelled", groupId = "delivery-service")
    public void consume(String payload) throws JsonProcessingException {
        OrderCancelledEvent event = objectMapper.readValue(payload, OrderCancelledEvent.class);
        log.debug("order.cancelled 이벤트 수신: orderId={}", event.getOrderId());
        deliveryService.cancelByOrderId(event.getOrderId());
    }
}
