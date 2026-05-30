package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.infrastructure.client.dto.OrderCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * OrderEventConsumer 테스트
 *
 * 목적:
 * - Kafka에서 order.created 이벤트를 수신했을 때
 *   Slack AI 알림 처리 로직이 정상 호출되는지 검증한다.
 *
 * 테스트 방식:
 * - 실제 Kafka Broker를 띄우지 않는다.
 * - @KafkaListener가 붙은 consumeOrderCreatedEvent() 메서드를 직접 호출한다.
 * - AiMessageService는 Mock으로 대체한다.
 */
class OrderEventConsumerTest {

    private final AiMessageService aiMessageService = Mockito.mock(AiMessageService.class);
    private final OrderEventConsumer orderEventConsumer = new OrderEventConsumer(aiMessageService);

    @Test
    @DisplayName("주문 생성 이벤트 수신 시 AI 메시지 알림 처리를 호출한다")
    void consumeOrderCreatedEvent_success() {
        // given
        UUID orderId = UUID.randomUUID();

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .build();

        // when
        orderEventConsumer.consumeOrderCreatedEvent(event);

        // then
        verify(aiMessageService, times(1)).processOrderNotification(orderId);
    }

    @Test
    @DisplayName("AI 메시지 알림 처리 중 예외가 발생해도 Consumer 메서드는 예외를 밖으로 던지지 않는다")
    void consumeOrderCreatedEvent_exception_swallowed() {
        // given
        UUID orderId = UUID.randomUUID();

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .build();

        doThrow(new RuntimeException("AI 알림 처리 실패"))
                .when(aiMessageService)
                .processOrderNotification(orderId);

        // when & then
        // Consumer 내부에서 try-catch로 예외를 처리하므로 테스트가 실패하지 않아야 한다.
        orderEventConsumer.consumeOrderCreatedEvent(event);

        verify(aiMessageService, times(1)).processOrderNotification(orderId);
    }
}
