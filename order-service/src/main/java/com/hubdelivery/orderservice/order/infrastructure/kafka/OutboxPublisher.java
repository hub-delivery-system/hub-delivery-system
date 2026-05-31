package com.hubdelivery.orderservice.order.infrastructure.kafka;

import com.hubdelivery.orderservice.order.domain.entity.OutboxEvent;
import com.hubdelivery.orderservice.order.domain.repository.OutboxEventRepository;
import com.hubdelivery.orderservice.order.domain.type.OutboxEventStatus;
import java.util.List;
import java.util.Map;
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

    private static final Map<String, String> EVENT_TOPIC_MAP = Map.of(
            "ORDER_CREATED", "order.created",
            "ORDER_CANCELLED", "order.cancelled"
    );

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 5초마다 PENDING·FAILED 이벤트를 Kafka에 발행한다 (FAILED는 재시도)
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusIn(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED));
        for (OutboxEvent event : pending) {
            String topic = EVENT_TOPIC_MAP.get(event.getEventType());
            if (topic == null) {
                log.warn("알 수 없는 이벤트 타입: eventId={}, type={}", event.getId(), event.getEventType());
                event.markFailed();
                continue;
            }
            try {
                kafkaTemplate.send(topic, event.getAggregateId().toString(), event.getPayload()).get();
                event.markPublished();
                log.debug("Outbox 이벤트 발행 완료: eventId={}, topic={}", event.getId(), topic);
            } catch (Exception e) {
                log.error("Outbox 이벤트 발행 실패: eventId={}, topic={}", event.getId(), topic, e);
                event.markFailed();
            }
        }
    }
}
