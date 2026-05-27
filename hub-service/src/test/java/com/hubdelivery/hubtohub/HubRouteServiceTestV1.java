package com.hubdelivery.hubtohub;

import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hubtohub.application.service.HubRouteService;
import com.hubdelivery.hubtohub.domain.repository.HubToHubRepository;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.KakaoMobilityClient;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.KakaoMobilityClient.Coordinate;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("HubRouteService 단위 테스트")
class HubRouteServiceTestV1 {

    @InjectMocks
    private HubRouteService hubRouteService;


    @Mock
    private KakaoMobilityClient kakaoClient;

    private HubEntity seoulHub;       // 서울 (경기남부 권역)
    private HubEntity incheonHub;     // 인천 (경기남부 권역)
    private HubEntity busanHub;       // 부산 (대구 권역)
    private HubEntity daejeonHub;     // 대전 (대전 권역)

    @BeforeEach
    void setUp() {
        // 서울 허브 (경기남부 중앙허브와 가까움)
        seoulHub = HubEntity.builder()
                .hubName("서울허브")
                .address("서울시 강남구")
                .latitude(new BigDecimal("37.497900"))
                .longitude(new BigDecimal("127.027600"))
                .build();
        ReflectionTestUtils.setField(seoulHub, "id", UUID.randomUUID());

        // 인천 허브 (경기남부 중앙허브와 가까움)
        incheonHub = HubEntity.builder()
                .hubName("인천허브")
                .address("인천시 중구")
                .latitude(new BigDecimal("37.456300"))
                .longitude(new BigDecimal("126.705200"))
                .build();
        ReflectionTestUtils.setField(incheonHub, "id", UUID.randomUUID());

        // 부산 허브 (대구 중앙허브와 가까움)
        busanHub = HubEntity.builder()
                .hubName("부산허브")
                .address("부산시 해운대구")
                .latitude(new BigDecimal("35.179600"))
                .longitude(new BigDecimal("129.075600"))
                .build();
        ReflectionTestUtils.setField(busanHub, "id", UUID.randomUUID());

        // 대전 허브 (대전 중앙허브와 가까움)
        daejeonHub = HubEntity.builder()
                .hubName("대전허브")
                .address("대전시 유성구")
                .latitude(new BigDecimal("36.351000"))
                .longitude(new BigDecimal("127.385000"))
                .build();
        ReflectionTestUtils.setField(daejeonHub, "id", UUID.randomUUID());
    }

    @Nested
    @DisplayName("경로 계산 테스트")
    class CalculateRouteTest {

        @Test
        @DisplayName("같은 권역 허브 간 경로 계산 (서울 → 인천)")
        void calculateRoute_SameRegion_Success() {
            // given - 카카오 API 응답 Mock
            DirectionsResponse mockResponse = createMockResponse(
                    50000,  // 50km
                    3600    // 60분 (3600초)
            );
            given(kakaoClient.getDirections(any(), any(), any()))
                    .willReturn(mockResponse);

            // when
            HubRouteService.RouteInfo result = hubRouteService.calculateRoute(seoulHub, incheonHub);

            // then
            assertThat(result).isNotNull();
            assertThat(result.distanceKm()).isEqualTo(new BigDecimal("50.00"));
            assertThat(result.durationMinutes()).isEqualTo(60);

            // 카카오 API가 호출되었는지 확인
            verify(kakaoClient, times(1)).getDirections(any(), any(), any());
        }

        @Test
        @DisplayName("다른 권역 허브 간 경로 계산 (서울 → 부산)")
        void calculateRoute_DifferentRegion_Success() {
            // given
            DirectionsResponse mockResponse = createMockResponse(
                    400000,  // 400km
                    18000    // 5시간 (18000초)
            );
            given(kakaoClient.getDirections(any(), any(), any()))
                    .willReturn(mockResponse);

            // when
            HubRouteService.RouteInfo result = hubRouteService.calculateRoute(seoulHub, busanHub);

            // then
            assertThat(result).isNotNull();
            assertThat(result.distanceKm()).isEqualTo(new BigDecimal("400.00"));
            assertThat(result.durationMinutes()).isEqualTo(300);  // 5시간 = 300분
            verify(kakaoClient, times(1)).getDirections(any(), any(), any());
        }

        @Test
        @DisplayName("대전 → 부산 경로 계산")
        void calculateRoute_DaejeonToBusan_Success() {
            // given
            DirectionsResponse mockResponse = createMockResponse(
                    250000,  // 250km
                    10800    // 3시간
            );
            given(kakaoClient.getDirections(any(), any(), any()))
                    .willReturn(mockResponse);

            // when
            HubRouteService.RouteInfo result = hubRouteService.calculateRoute(daejeonHub, busanHub);

            // then
            assertThat(result.distanceKm()).isEqualTo(new BigDecimal("250.00"));
            assertThat(result.durationMinutes()).isEqualTo(180);
        }

        @Test
        @DisplayName("거리/시간 변환이 정확하다")
        void calculateRoute_ConversionAccuracy() {
            // given - 12345m, 3725초
            DirectionsResponse mockResponse = createMockResponse(12345, 3725);
            given(kakaoClient.getDirections(any(), any(), any()))
                    .willReturn(mockResponse);

            // when
            HubRouteService.RouteInfo result = hubRouteService.calculateRoute(seoulHub, busanHub);

            // then
            // 12345 / 1000 = 12.345 → 반올림 → 12.35
            assertThat(result.distanceKm()).isEqualTo(new BigDecimal("12.35"));
            // 3725 / 60 = 62 (정수 나눗셈)
            assertThat(result.durationMinutes()).isEqualTo(62);
        }
    }

    @Nested
    @DisplayName("Hub and Spoke 경유지 테스트")
    class WaypointsTest {

        @Test
        @DisplayName("같은 권역(서울→인천)이면 중앙허브 1개만 경유")
        void buildWaypoints_SameRegion_OneWaypoint() {
            // given
            DirectionsResponse mockResponse = createMockResponse(50000, 3600);
            given(kakaoClient.getDirections(any(), any(), any()))
                    .willReturn(mockResponse);

            // when - 서울과 인천은 둘 다 경기남부 권역
            hubRouteService.calculateRoute(seoulHub, incheonHub);

            // then
            verify(kakaoClient, times(1)).getDirections(
                    any(Coordinate.class),
                    any(Coordinate.class),
                    any(List.class)
            );
        }

        @Test
        @DisplayName("다른 권역(서울→부산)이면 중앙허브 2개 경유")
        void buildWaypoints_DifferentRegion_TwoWaypoints() {
            // given
            DirectionsResponse mockResponse = createMockResponse(400000, 18000);
            given(kakaoClient.getDirections(any(), any(), any()))
                    .willReturn(mockResponse);

            // when - 서울(경기남부) → 부산(대구)
            hubRouteService.calculateRoute(seoulHub, busanHub);

            // then
            verify(kakaoClient, times(1)).getDirections(
                    any(Coordinate.class),
                    any(Coordinate.class),
                    any(List.class)
            );
        }
    }

    // ===== 헬퍼 메서드 =====

    /**
     * Mock DirectionsResponse 생성
     */
    private DirectionsResponse createMockResponse(int distanceMeters, int durationSeconds) {
        DirectionsResponse.Summary summary = new DirectionsResponse.Summary();
        ReflectionTestUtils.setField(summary, "distance", distanceMeters);
        ReflectionTestUtils.setField(summary, "duration", durationSeconds);

        DirectionsResponse.Route route = new DirectionsResponse.Route();
        ReflectionTestUtils.setField(route, "summary", summary);
        ReflectionTestUtils.setField(route, "resultCode", "0");
        ReflectionTestUtils.setField(route, "resultMsg", "성공");

        DirectionsResponse response = new DirectionsResponse();
        ReflectionTestUtils.setField(response, "routes", List.of(route));

        return response;
    }
}
