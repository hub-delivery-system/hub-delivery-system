package com.hubdelivery.deliveryservice.delivery.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubdelivery.common.event.DeliveryCreatedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventProducer {

    private static final String TOPIC_DELIVERY_CREATED = "delivery.created";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishDeliveryCreated(UUID orderId, UUID deliveryId) {
        DeliveryCreatedEvent event = DeliveryCreatedEvent.builder()
                .orderId(orderId)
                .deliveryId(deliveryId)
                .build();
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_DELIVERY_CREATED, orderId.toString(), payload);
            log.debug("delivery.created 이벤트 발행: orderId={}, deliveryId={}", orderId, deliveryId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("DeliveryCreatedEvent 직렬화 실패", e);
        }
    }
}
