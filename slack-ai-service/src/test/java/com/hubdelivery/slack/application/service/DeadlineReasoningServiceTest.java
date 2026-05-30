package com.hubdelivery.slack.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DeadlineReasoningServiceTest {

    private final DeadlineReasoningService deadlineReasoningService = new DeadlineReasoningService();

    @Test
    @DisplayName("장거리 배송이면 장거리 버퍼가 추가된다")
    void calculate_longDistance_addsExtraBuffer() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 5, 30, 10, 0);
        LocalDateTime dueAt = LocalDateTime.of(2026, 5, 31, 18, 0);

        // when
        DeadlineReasoningResult result = deadlineReasoningService.calculate(
                now,
                dueAt,
                150,
                240,
                2
        );

        // then
        assertThat(result.longDistance()).isTrue();
        assertThat(result.bufferMinutes()).isEqualTo(60 + 120 + 60);
        assertThat(result.reasons()).anyMatch(reason -> reason.contains("장거리 배송 버퍼"));
    }

    @Test
    @DisplayName("경유 허브 개수만큼 추가 버퍼가 반영된다")
    void calculate_waypoint_addsBuffer() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 5, 30, 10, 0);
        LocalDateTime dueAt = LocalDateTime.of(2026, 5, 31, 18, 0);

        // when
        DeadlineReasoningResult result = deadlineReasoningService.calculate(
                now,
                dueAt,
                50,
                180,
                3
        );

        // then
        assertThat(result.bufferMinutes()).isEqualTo(60 + 90);
        assertThat(result.waypointCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("권장 발송 시각이 근무시간 이전이면 09시로 보정된다")
    void calculate_beforeWorkStart_adjustsToNine() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 5, 30, 1, 0);
        LocalDateTime dueAt = LocalDateTime.of(2026, 5, 30, 7, 0);

        // when
        DeadlineReasoningResult result = deadlineReasoningService.calculate(
                now,
                dueAt,
                20,
                60,
                0
        );

        // then
        assertThat(result.recommendedSendAt().toLocalTime().getHour()).isEqualTo(9);
        assertThat(result.reasons()).anyMatch(reason -> reason.contains("근무 시작 전"));
    }
}
