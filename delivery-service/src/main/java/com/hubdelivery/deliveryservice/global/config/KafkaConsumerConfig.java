package com.hubdelivery.deliveryservice.global.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    static final String DLT_SUFFIX = "-dlt";
    private static final long RETRY_INTERVAL_MS = 1000L;
    private static final long MAX_RETRIES = 3L;

    // Spring Boot가 기본 리스너 컨테이너 팩토리에 자동 적용한다
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        // 파티션 -1: DLT 파티션 수가 원본과 달라도 발행되도록 Kafka가 파티션을 고르게 한다
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> {
                    log.error("이벤트 처리 최종 실패 — DLT로 이동: topic={}, key={}",
                            record.topic(), record.key(), ex);
                    return new TopicPartition(record.topic() + DLT_SUFFIX, -1);
                });

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES));
        // 역직렬화 실패는 재시도해도 성공할 수 없으므로 즉시 DLT로 보낸다
        errorHandler.addNotRetryableExceptions(JsonProcessingException.class);
        return errorHandler;
    }
}
