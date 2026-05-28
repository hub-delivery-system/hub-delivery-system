package com.hubdelivery.hubtohub;

import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hubtohub.application.service.HubRouteService;
import com.hubdelivery.hubtohub.domain.repository.HubToHubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@DisplayName("카카오 API 실제 호출 테스트")
class KakaoMobilityClientIntegrationTest {

    @Autowired
    private HubRouteService hubRouteService;

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private HubToHubRepository hubRouteRepository;

    private HubEntity seoulHub;
    private HubEntity incheonHub;
    private HubEntity busanHub;
    private HubEntity daejeonHub;

    @BeforeEach
    void setUp() {
        // 단위 테스트와 달리 빌더 패턴으로 데이터를 직접 DB에 저장(save)하여 가짜 ID가 아닌 실제 식별자를 부여합니다.
        Pageable pageable = PageRequest.of(0, 1);

        // 1. 서울허브
        seoulHub = hubRepository.searchHubs("서울허브", pageable)
                .stream()                  // Page 안의 콘텐츠를 Stream으로 변환
                .findFirst()               // 첫 번째 요소 추출 (Optional<HubEntity> 반환)
                .orElseGet(() -> hubRepository.save(HubEntity.builder()
                        .hubName("서울허브")
                        .address("서울시 강남구")
                        .latitude(new BigDecimal("37.497900"))
                        .longitude(new BigDecimal("127.027600"))
                        .build()));

        // 2. 인천허브
        incheonHub = hubRepository.searchHubs("인천허브", pageable)
                .stream()
                .findFirst()
                .orElseGet(() -> hubRepository.save(HubEntity.builder()
                        .hubName("인천허브")
                        .address("인천시 중구")
                        .latitude(new BigDecimal("37.456300"))
                        .longitude(new BigDecimal("126.705200"))
                        .build()));

        // 3. 부산허브
        busanHub = hubRepository.searchHubs("부산허브", pageable)
                .stream()
                .findFirst()
                .orElseGet(() -> hubRepository.save(HubEntity.builder()
                        .hubName("부산허브")
                        .address("부산시 해운대구")
                        .latitude(new BigDecimal("35.179600"))
                        .longitude(new BigDecimal("129.075600"))
                        .build()));

        // 4. 대전허브
        daejeonHub = hubRepository.searchHubs("대전허브", pageable)
                .stream()
                .findFirst()
                .orElseGet(() -> hubRepository.save(HubEntity.builder()
                        .hubName("대전허브")
                        .address("대전시 유성구")
                        .latitude(new BigDecimal("36.351000"))
                        .longitude(new BigDecimal("127.385000"))
                        .build()));
    }

    @Nested
    @DisplayName("실제 인프라를 통한 경로 계산")
    class RealCalculateRouteTest {

        @Test
        @DisplayName("같은 권역 허브 간 실제 경로 계산 (서울 → 인천)")
        void calculateRoute_SameRegion_RealCall() {
            // when - Mock 없이 실제 빈(HubRouteService -> RestClient -> Kakao API) 호출
            HubRouteService.RouteInfo result = hubRouteService.calculateRoute(seoulHub, incheonHub);

            // then
            assertThat(result).isNotNull();
            assertThat(result.distanceKm()).isGreaterThan(BigDecimal.ZERO);
            assertThat(result.durationMinutes()).isGreaterThan(0);

            System.out.println("====== [통합 테스트 결과: 서울 -> 인천] ======");
            System.out.println("📏 실제 계산 거리: " + result.distanceKm() + " km");
            System.out.println("⏱️ 실제 소요 시간: " + result.durationMinutes() + " 분");
        }

        @Test
        @DisplayName("다른 권역 허브 간 실제 Hub and Spoke 경로 계산 (서울 → 부산)")
        void calculateRoute_DifferentRegion_RealCall() {
            // when - 서울 -> (경기남부 허브 -> 대구 허브 경유 예상) -> 부산 경로 계산 실행
            HubRouteService.RouteInfo result = hubRouteService.calculateRoute(seoulHub, busanHub);

            // then
            assertThat(result).isNotNull();
            // 서울-부산은 최소 300km 이상이므로 비즈니스 로직 및 API 연동 결과 검증
            assertThat(result.distanceKm()).isGreaterThan(new BigDecimal("300.00"));
            assertThat(result.durationMinutes()).isGreaterThan(180); // 3시간 이상

            System.out.println("====== [통합 테스트 결과: 서울 -> 부산] ======");
            System.out.println("📏 실제 경유 거리: " + result.distanceKm() + " km");
            System.out.println("⏱️ 실제 경유 시간: " + result.durationMinutes() + " 분");
        }

        @Test
        @DisplayName("대전 → 부산 실제 경로 계산")
        void calculateRoute_DaejeonToBusan_RealCall() {
            // when
            HubRouteService.RouteInfo result = hubRouteService.calculateRoute(daejeonHub, busanHub);

            // then
            assertThat(result).isNotNull();
            assertThat(result.distanceKm()).isGreaterThan(new BigDecimal("200.00"));

            System.out.println("====== [통합 테스트 결과: 대전 -> 부산] ======");
            System.out.println("📏 실제 계산 거리: " + result.distanceKm() + " km");
            System.out.println("⏱️ 실제 소요 시간: " + result.durationMinutes() + " 분");
        }
    }
}
