package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.infrastructure.client.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final AiMessageService aiMessageService;

    @KafkaListener(
            topics = "${kafka.topic.order-created:order.created}",
            groupId = "slack-ai-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트 수신 - orderId: {}", event.getOrderId());
        try {
            aiMessageService.processOrderNotification(event.getOrderId());
            log.info("주문 알림 처리 완료 - orderId: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("주문 알림 처리 실패 - orderId: {}, error: {}",
                    event.getOrderId(), e.getMessage());
        }
    }
}
