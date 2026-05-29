package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.infrastructure.client.DeliveryManagerClient;
import com.hubdelivery.slack.infrastructure.client.dto.CompanyDeliveryManagerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryAlertScheduler {

    private final DeliveryManagerClient deliveryManagerClient;
    private final NaverDirectionsService naverDirectionsService;
    private final GeminiService geminiService;
    private final SlackSendService slackSendService;

    @Scheduled(cron = "${scheduler.daily-alert.cron:0 0 6 * * *}")
    public void sendDailyDeliveryAlert() {
        log.info("일일 업체 배송담당자 슬랙 알림 스케줄러 시작");

        try {
            List<CompanyDeliveryManagerDto> managers =
                    deliveryManagerClient.getCompanyDeliveryManagers(
                            "COMPANY_DELIVERY_MANAGER", "system", "MASTER");

            if (managers == null || managers.isEmpty()) {
                log.info("오늘 배송 담당자 없음 - 알림 종료");
                return;
            }

            for (CompanyDeliveryManagerDto manager : managers) {
                try {
                    sendAlertToManager(manager);
                } catch (Exception e) {
                    log.error("담당자 알림 전송 실패 - managerId: {}, error: {}",
                            manager.getId(), e.getMessage());
                }
            }

            log.info("일일 업체 배송담당자 슬랙 알림 완료 - 총 {}명", managers.size());

        } catch (Exception e) {
            log.error("일일 알림 스케줄러 실패: {}", e.getMessage());
        }
    }

    private void sendAlertToManager(CompanyDeliveryManagerDto manager) {
        List<CompanyDeliveryManagerDto.DeliveryDestinationDto> destinations =
                manager.getDestinations();

        if (destinations == null || destinations.isEmpty()) {
            log.info("담당자 {} 오늘 배송 없음 - 스킵", manager.getId());
            return;
        }

        // 1. 네이버 Directions API로 최적 경로 계산
        NaverDirectionsService.NaverRouteResult routeResult =
                naverDirectionsService.getOptimalRoute(destinations);

        // 2. Gemini AI로 최적 배송 순서 메시지 생성
        String aiMessage;
        try {
            aiMessage = geminiService.generateDeadlineMessage(
                    buildPrompt(destinations, routeResult));
        } catch (Exception e) {
            log.error("Gemini 메시지 생성 실패 - managerId: {}", manager.getId());
            aiMessage = String.format("오늘 배송 건수: %d건. 순서 최적화를 수동으로 확인해주세요.",
                    destinations.size());
        }

        // 3. 슬랙 알림 전송
        String message = buildFinalMessage(manager, destinations, routeResult, aiMessage);
        boolean success = slackSendService.sendMessage(manager.getSlackId(), message);

        if (success) {
            log.info("담당자 {} 일일 알림 전송 성공", manager.getId());
        }
    }

    private String buildPrompt(
            List<CompanyDeliveryManagerDto.DeliveryDestinationDto> destinations,
            NaverDirectionsService.NaverRouteResult routeResult) {

        StringBuilder sb = new StringBuilder();
        sb.append("아래 배송 담당자의 오늘 배송 일정을 분석하여 최적 배송 순서와 예상 완료 시간을 알려주세요.\n");
        sb.append("담당자 근무시간: 09:00 ~ 18:00\n\n배송 목적지 목록:\n");

        for (int i = 0; i < destinations.size(); i++) {
            var dest = destinations.get(i);
            sb.append(String.format("%d. %s (위도: %.4f, 경도: %.4f)\n",
                    i + 1, dest.getAddress(), dest.getLatitude(), dest.getLongitude()));
        }

        if (!routeResult.isEmpty()) {
            sb.append(String.format("\n네이버 경로 분석: 총 %.1fkm, 예상 소요시간 %d분\n",
                    routeResult.getDistanceMeters() / 1000.0,
                    routeResult.getDurationSeconds() / 60));
        }

        sb.append("\n응답 형식: 최적 방문 순서와 예상 완료 시각을 간결하게 알려주세요.");
        return sb.toString();
    }

    private String buildFinalMessage(
            CompanyDeliveryManagerDto manager,
            List<CompanyDeliveryManagerDto.DeliveryDestinationDto> destinations,
            NaverDirectionsService.NaverRouteResult routeResult,
            String aiMessage) {

        StringBuilder sb = new StringBuilder();
        sb.append("🚚 *오늘의 배송 일정 알림*\n\n");
        sb.append(String.format("오늘 배송 건수: %d건\n\n", destinations.size()));

        sb.append("📍 배송 목적지:\n");
        for (int i = 0; i < destinations.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, destinations.get(i).getAddress()));
        }

        if (!routeResult.isEmpty()) {
            sb.append(String.format("\n🗺 총 이동거리: %.1fkm\n",
                    routeResult.getDistanceMeters() / 1000.0));
            sb.append(String.format("⏱ 예상 소요시간: %d분\n",
                    routeResult.getDurationSeconds() / 60));
        }

        sb.append("\n🤖 AI 최적 배송 순서:\n").append(aiMessage);
        return sb.toString();
    }
}
