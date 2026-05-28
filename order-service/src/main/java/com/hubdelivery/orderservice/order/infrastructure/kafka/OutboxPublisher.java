package com.hubdelivery.orderservice.order.infrastructure.kafka;

import com.hubdelivery.orderservice.order.domain.entity.OutboxEvent;
import com.hubdelivery.orderservice.order.domain.repository.OutboxEventRepository;
import com.hubdelivery.orderservice.order.domain.type.OutboxEventStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String TOPIC_ORDER_CREATED = "order.created";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 5초마다 PENDING·FAILED 이벤트를 Kafka에 발행한다 (FAILED는 재시도)
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusIn(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED));
        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(TOPIC_ORDER_CREATED, event.getAggregateId().toString(), event.getPayload()).get();
                event.markPublished();
                log.debug("Outbox 이벤트 발행 완료: eventId={}, orderId={}", event.getId(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Outbox 이벤트 발행 실패: eventId={}", event.getId(), e);
                event.markFailed();
            }
        }
    }
}
