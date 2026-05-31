package com.hubdelivery.hubtohub.application.service;


import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hubtohub.domain.repository.HubTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubTransferScheduler {

    private final HubRepository hubRepository;
    private final HubTransferRepository hubTransferRepository;
    private final HubTransferCreateService hubTransferCreateService;

    private static final int PAGE_SIZE = 100;
    private static final int MIN_DELAY_MS = 300;  // 최소 딜레이
    private static final int MAX_DELAY_MS = 700;  // 최대 딜레이 (jitter)

    /**
     * 매일 새벽 4시 - 경로가 없는 Hub 쌍에 대해 경로 생성
     */
    @Scheduled(cron = "0 0 4 * * *")
    //@Scheduled(cron = "0 * * * * *")// 테스트용
    public void createMissingHubRoutes() {
        log.info("===== [스케줄러 시작] Hub 경로 자동 생성 =====");

        // 모든 활성 Hub 조회
        List<HubEntity> allHubs = fetchAllActiveHubs();
        if (allHubs.size() < 2) {
            log.info("활성 Hub가 2개 미만이므로 스케줄러 종료");
            return;
        }
        log.info("활성 Hub 조회 완료 - 총 {}개", allHubs.size());

        int created = 0;
        int skipped = 0;
        int failed = 0;

        // 모든 Hub 쌍 조합 생성 (A→B, B→A)
        for (int i = 0; i < allHubs.size(); i++) {
            for (int j = 0; j < allHubs.size(); j++) {
                if (i == j) continue;

                HubEntity fromHub = allHubs.get(i);
                HubEntity toHub = allHubs.get(j);

                // 이미 경로가 있는 쌍 제외
                boolean exists = hubTransferRepository
                        .existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(
                                fromHub.getId(), toHub.getId()
                        );

                if (exists) {
                    skipped++;
                    continue;
                }

                // 경로 생성
                try {
                    hubTransferCreateService.createAndSaveNewRoute(fromHub, toHub);
                    log.info("경로 생성 완료 - {} → {}", fromHub.getHubName(), toHub.getHubName());
                    created++;

                    // jitter 딜레이 (카카오 API 호출 제한 고려)
                    applyJitterDelay();

                } catch (Exception e) {
                    // 실패 시 로그만 남기고 다음 경로로
                    log.warn("경로 생성 실패 - {} → {} : {}",
                            fromHub.getHubName(), toHub.getHubName(), e.getMessage());
                    failed++;
                }
            }
        }

        log.info("===== [스케줄러 완료] 생성: {}개, 스킵: {}개, 실패: {}개 =====",
                created, skipped, failed);
    }

    /**
     * 모든 활성 Hub 페이지네이션으로 전체 조회
     */
    private List<HubEntity> fetchAllActiveHubs() {
        List<HubEntity> allHubs = new ArrayList<>();
        int page = 0;

        while (true) {
            Page<HubEntity> hubPage = hubRepository.findAllActive(
                    PageRequest.of(page, PAGE_SIZE)
            );
            allHubs.addAll(hubPage.getContent());

            if (!hubPage.hasNext()) break;
            page++;
        }

        return allHubs;
    }

    /**
     * Jitter 딜레이 (300ms ~ 700ms 랜덤)
     * 카카오 API 호출 제한 방지
     */
    private void applyJitterDelay() {
        try {
            long delay = ThreadLocalRandom.current()
                    .nextLong(MIN_DELAY_MS, MAX_DELAY_MS);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("스케줄러 딜레이 중 인터럽트 발생");
        }
    }
}
