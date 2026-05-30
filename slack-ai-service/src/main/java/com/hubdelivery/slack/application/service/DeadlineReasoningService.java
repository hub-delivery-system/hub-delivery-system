package com.hubdelivery.slack.application.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 추론 정확도 향상을 위한 서버 측 발송 시한 계산 서비스.
 *
 * 핵심 의도:
 * - Gemini가 배송 시간을 직접 추측하지 않도록 한다.
 * - 서버가 거리, 예상 소요시간, 경유지 수, 근무시간을 기준으로 계산한다.
 * - Gemini는 계산 결과를 자연어로 정리하는 역할만 담당한다.
 */
@Service
public class DeadlineReasoningService {

    private static final LocalTime WORK_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime WORK_END_TIME = LocalTime.of(18, 0);

    private static final long DEFAULT_PREPARATION_BUFFER_MINUTES = 60;
    private static final long LONG_DISTANCE_EXTRA_BUFFER_MINUTES = 120;
    private static final long WAYPOINT_EXTRA_BUFFER_MINUTES = 30;

    private static final int LONG_DISTANCE_THRESHOLD_KM = 100;

    /**
     * @param now 현재 시간
     * @param dueAt 고객 요청 납기 시각
     * @param estimatedDistanceKm 예상 총 거리(km)
     * @param estimatedDurationMinutes 예상 총 소요시간(분)
     * @param waypointCount 경유 허브 개수
     */
    public DeadlineReasoningResult calculate(
            LocalDateTime now,
            LocalDateTime dueAt,
            int estimatedDistanceKm,
            int estimatedDurationMinutes,
            int waypointCount
    ) {
        List<String> reasons = new ArrayList<>();

        boolean longDistance = estimatedDistanceKm >= LONG_DISTANCE_THRESHOLD_KM;

        long bufferMinutes = DEFAULT_PREPARATION_BUFFER_MINUTES;
        reasons.add("기본 배송 준비 버퍼 60분을 적용했습니다.");

        if (longDistance) {
            bufferMinutes += LONG_DISTANCE_EXTRA_BUFFER_MINUTES;
            reasons.add("예상 거리가 100km 이상이므로 장거리 배송 버퍼 120분을 추가했습니다.");
        }

        if (waypointCount > 0) {
            long waypointBuffer = (long) waypointCount * WAYPOINT_EXTRA_BUFFER_MINUTES;
            bufferMinutes += waypointBuffer;
            reasons.add("경유 허브 " + waypointCount + "개에 대해 허브당 30분의 버퍼를 추가했습니다.");
        }

        LocalDateTime recommendedSendAt = dueAt
                .minusMinutes(estimatedDurationMinutes)
                .minusMinutes(bufferMinutes);

        reasons.add("요청 납기 시각에서 예상 소요시간과 총 버퍼 시간을 차감해 권장 발송 시각을 계산했습니다.");

        LocalDateTime adjusted = adjustToWorkingTime(recommendedSendAt, reasons);

        if (adjusted.isBefore(now)) {
            reasons.add("계산된 권장 발송 시각이 현재 시각보다 이전이므로 즉시 발송이 필요한 건으로 판단했습니다.");
            adjusted = now;
        }

        return new DeadlineReasoningResult(
                adjusted,
                estimatedDurationMinutes,
                bufferMinutes,
                longDistance,
                waypointCount,
                reasons
        );
    }

    /**
     * 배송 담당자 근무시간 09:00~18:00 기준 보정.
     */
    private LocalDateTime adjustToWorkingTime(LocalDateTime dateTime, List<String> reasons) {
        LocalTime time = dateTime.toLocalTime();

        if (time.isBefore(WORK_START_TIME)) {
            reasons.add("권장 발송 시각이 근무 시작 전이므로 당일 09:00로 보정했습니다.");
            return LocalDateTime.of(dateTime.toLocalDate(), WORK_START_TIME);
        }

        if (time.isAfter(WORK_END_TIME)) {
            reasons.add("권장 발송 시각이 근무 종료 후이므로 다음 영업일 09:00로 보정했습니다.");
            return LocalDateTime.of(dateTime.toLocalDate().plusDays(1), WORK_START_TIME);
        }

        reasons.add("권장 발송 시각이 배송 담당자 근무시간(09:00~18:00) 내에 있습니다.");
        return dateTime;
    }
}
