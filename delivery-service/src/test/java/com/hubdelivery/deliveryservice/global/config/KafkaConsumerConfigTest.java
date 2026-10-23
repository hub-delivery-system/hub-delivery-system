package com.hubdelivery.deliveryservice.global.config;

import com.fasterxml.jackson.core.JsonParseException;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.SendResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class KafkaConsumerConfigTest {

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("역직렬화 실패 이벤트는 재시도 없이 즉시 <원본 토픽>-dlt로 이동한다")
    void jsonFailure_goesToDltWithoutRetry() {
        // given
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        given(kafkaTemplate.send(any(ProducerRecord.class)))
                .willReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        DefaultErrorHandler errorHandler = new KafkaConsumerConfig().kafkaErrorHandler(kafkaTemplate);

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("order.created", 0, 0L, "order-1", "not-json");
        // 실제 컨테이너처럼 리스너 예외를 ListenerExecutionFailedException으로 감싼다
        Exception thrown = new ListenerExecutionFailedException("listener failed",
                new JsonParseException(null, "invalid json"));

        // when — 첫 실패에서 바로 복구(DLT 발행)되면 true를 반환한다
        boolean recovered = errorHandler.handleOne(thrown, record, mock(Consumer.class),
                mock(MessageListenerContainer.class));

        // then
        assertThat(recovered).isTrue();
        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        then(kafkaTemplate).should().send(captor.capture());
        assertThat(captor.getValue().topic()).isEqualTo("order.created-dlt");
        assertThat(captor.getValue().value()).isEqualTo("not-json");
    }
}
