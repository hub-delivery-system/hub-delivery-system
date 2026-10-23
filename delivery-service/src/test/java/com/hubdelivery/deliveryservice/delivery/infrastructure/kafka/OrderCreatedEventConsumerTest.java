package com.hubdelivery.deliveryservice.delivery.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubdelivery.deliveryservice.delivery.application.DeliveryService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

class OrderCreatedEventConsumerTest {

    private final DeliveryService deliveryService = mock(DeliveryService.class);
    private final OrderCreatedEventConsumer consumer =
            new OrderCreatedEventConsumer(deliveryService, new ObjectMapper());

    @Test
    @DisplayName("역직렬화 실패 시 예외를 삼키지 않고 에러 핸들러로 전파한다")
    void consume_invalidPayload_throws() {
        assertThatThrownBy(() -> consumer.consume("not-json"))
                .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    @DisplayName("배송 생성 실패 시 예외를 삼키지 않고 에러 핸들러로 전파한다")
    void consume_serviceFailure_throws() {
        willThrow(new IllegalStateException("DB 장애")).given(deliveryService).createFromEvent(any());

        assertThatThrownBy(() -> consumer.consume("{\"orderId\":\"" + UUID.randomUUID() + "\"}"))
                .isInstanceOf(IllegalStateException.class);
    }
}
