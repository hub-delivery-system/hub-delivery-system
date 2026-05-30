package com.hubdelivery.slack.application.service;

/**
 * 이 파일은 예시입니다.
 *
 * 기존 OrderEventConsumer에 KafkaTraceLogger를 주입하고,
 * 이벤트 처리 시작/종료 시점에 trace 로그를 남기는 방식으로 이식하세요.
 */
public class OrderEventConsumerIntegrationExample {

    /*
    private final AiMessageService aiMessageService;
    private final KafkaTraceLogger kafkaTraceLogger;

    @KafkaListener(
            topics = "${kafka.topic.order-created:order.created}",
            groupId = "slack-ai-service"
    )
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTraceLogger.logCurrentTrace("ORDER_EVENT_CONSUME_START", event.getOrderId());

        aiMessageService.processOrderNotification(event.getOrderId());

        kafkaTraceLogger.logCurrentTrace("ORDER_EVENT_CONSUME_END", event.getOrderId());
    }
    */
}
