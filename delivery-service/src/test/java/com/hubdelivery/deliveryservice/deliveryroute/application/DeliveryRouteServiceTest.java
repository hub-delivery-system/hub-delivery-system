package com.hubdelivery.deliveryservice.deliveryroute.application;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.deliveryservice.delivery.domain.entity.Delivery;
import com.hubdelivery.deliveryservice.delivery.domain.repository.DeliveryRepository;
import com.hubdelivery.deliveryservice.delivery.domain.type.DeliveryStatus;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.UserServiceClient;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.dto.UserResponse;
import com.hubdelivery.deliveryservice.deliveryroute.domain.entity.DeliveryRoute;
import com.hubdelivery.deliveryservice.deliveryroute.domain.exception.DeliveryRouteErrorCode;
import com.hubdelivery.deliveryservice.deliveryroute.domain.exception.DeliveryRouteException;
import com.hubdelivery.deliveryservice.deliveryroute.domain.repository.DeliveryRouteRepository;
import com.hubdelivery.deliveryservice.deliveryroute.domain.type.DeliveryRouteStatus;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteResponse;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DeliveryRouteServiceTest {

    @InjectMocks
    private DeliveryRouteService deliveryRouteService;

    @Mock
    private DeliveryRouteRepository deliveryRouteRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private UserServiceClient userServiceClient;

    // -----------------------------------------------------------------------
    // 테스트 헬퍼
    // -----------------------------------------------------------------------

    private DeliveryRoute buildRoute(UUID id, DeliveryRouteStatus status,
                                     UUID startHubId, UUID endHubId, UUID managerId) {
        DeliveryRoute route = DeliveryRoute.builder()
                .deliveryId(UUID.randomUUID())
                .sequence(1)
                .startHubId(startHubId)
                .endHubId(endHubId)
                .estimatedDistance(BigDecimal.valueOf(100.0))
                .estimatedDuration(60)
                .status(status)
                .deliveryManagerId(managerId)
                .build();
        try {
            var field = DeliveryRoute.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(route, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return route;
    }

    private Delivery buildDelivery(UUID id) {
        Delivery d = Delivery.builder()
                .orderId(UUID.randomUUID())
                .status(DeliveryStatus.HUB_PENDING)
                .startHubId(UUID.randomUUID())
                .endHubId(UUID.randomUUID())
                .address("서울시 강남구")
                .userId(UUID.randomUUID())
                .slackId("slack-test")
                .build();
        try {
            var field = Delivery.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(d, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return d;
    }

    private DeliveryRouteUpdateRequest buildUpdateRequest(DeliveryRouteStatus status) {
        try {
            var request = new DeliveryRouteUpdateRequest();
            var field = DeliveryRouteUpdateRequest.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(request, status);
            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // 상태 전이 테스트
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("WAITING_AT_HUB → IN_TRANSIT 전이 성공")
        void update_waitingToInTransit_succeeds() {
            // given
            UUID routeId = UUID.randomUUID();
            UUID managerId = UUID.randomUUID();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.WAITING_AT_HUB,
                    UUID.randomUUID(), UUID.randomUUID(), managerId);
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.IN_TRANSIT);

            // when
            DeliveryRouteResponse response = deliveryRouteService.update(
                    routeId, request, managerId.toString(), UserRole.DELIVERY_MANAGER);

            // then
            assertThat(response.getStatus()).isEqualTo(DeliveryRouteStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("IN_TRANSIT → ARRIVED_AT_HUB 전이 성공")
        void update_inTransitToArrived_succeeds() {
            // given
            UUID routeId = UUID.randomUUID();
            UUID managerId = UUID.randomUUID();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.IN_TRANSIT,
                    UUID.randomUUID(), UUID.randomUUID(), managerId);
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.ARRIVED_AT_HUB);

            // when
            DeliveryRouteResponse response = deliveryRouteService.update(
                    routeId, request, managerId.toString(), UserRole.DELIVERY_MANAGER);

            // then
            assertThat(response.getStatus()).isEqualTo(DeliveryRouteStatus.ARRIVED_AT_HUB);
        }

        @Test
        @DisplayName("WAITING_AT_HUB → ARRIVED_AT_HUB 단계 건너뜀 시 INVALID_STATUS_TRANSITION 예외 발생")
        void update_skipStep_throwsInvalidTransition() {
            // given
            UUID routeId = UUID.randomUUID();
            UUID managerId = UUID.randomUUID();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.WAITING_AT_HUB,
                    UUID.randomUUID(), UUID.randomUUID(), managerId);
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.ARRIVED_AT_HUB);

            // when & then
            assertThatThrownBy(() ->
                    deliveryRouteService.update(routeId, request, managerId.toString(), UserRole.DELIVERY_MANAGER))
                    .isInstanceOf(DeliveryRouteException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryRouteErrorCode.INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("DELIVERED 에서 다른 상태로 전이 불가 (선형 전이 끝)")
        void update_fromDelivered_throwsInvalidTransition() {
            // given
            UUID routeId = UUID.randomUUID();
            UUID managerId = UUID.randomUUID();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.DELIVERED,
                    UUID.randomUUID(), UUID.randomUUID(), managerId);
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            // DELIVERED → OUT_FOR_DELIVERY 역방향 전이 시도
            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.OUT_FOR_DELIVERY);

            // when & then
            assertThatThrownBy(() ->
                    deliveryRouteService.update(routeId, request, managerId.toString(), UserRole.DELIVERY_MANAGER))
                    .isInstanceOf(DeliveryRouteException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryRouteErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    // -----------------------------------------------------------------------
    // 권한 제어 테스트
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("권한 제어")
    class Authorization {

        @Test
        @DisplayName("MASTER는 모든 경로 수정 가능")
        void update_masterRole_succeeds() {
            // given
            UUID routeId = UUID.randomUUID();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.WAITING_AT_HUB,
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.IN_TRANSIT);

            // when & then — 예외 없이 수정 가능
            var response = deliveryRouteService.update(routeId, request, UUID.randomUUID().toString(), UserRole.MASTER);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("HUB_MANAGER는 담당 허브(start 또는 end)에 속한 경로 수정 가능")
        void update_hubManagerOwnHub_succeeds() {
            // given
            UUID routeId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            String userId = UUID.randomUUID().toString();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.WAITING_AT_HUB,
                    hubId, UUID.randomUUID(), UUID.randomUUID()); // startHubId = 담당 허브
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            UserResponse userResponse = mock(UserResponse.class);
            given(userResponse.getHubId()).willReturn(hubId);
            given(userServiceClient.getUser(any())).willReturn(ApiResponse.ok(userResponse));

            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.IN_TRANSIT);

            // when & then
            var response = deliveryRouteService.update(routeId, request, userId, UserRole.HUB_MANAGER);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("HUB_MANAGER가 담당 허브가 아닌 경로 수정 시 ROUTE_FORBIDDEN 예외 발생")
        void update_hubManagerOtherHub_throwsForbidden() {
            // given
            UUID routeId = UUID.randomUUID();
            UUID managerHubId = UUID.randomUUID();
            UUID otherHubId = UUID.randomUUID();
            String userId = UUID.randomUUID().toString();
            // startHubId, endHubId 모두 담당자 허브가 아님
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.WAITING_AT_HUB,
                    otherHubId, UUID.randomUUID(), UUID.randomUUID());
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            UserResponse userResponse = mock(UserResponse.class);
            given(userResponse.getHubId()).willReturn(managerHubId);
            given(userServiceClient.getUser(any())).willReturn(ApiResponse.ok(userResponse));

            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.IN_TRANSIT);

            // when & then
            assertThatThrownBy(() ->
                    deliveryRouteService.update(routeId, request, userId, UserRole.HUB_MANAGER))
                    .isInstanceOf(DeliveryRouteException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryRouteErrorCode.ROUTE_FORBIDDEN);
        }

        @Test
        @DisplayName("DELIVERY_MANAGER는 본인이 담당하는 경로 수정 가능")
        void update_deliveryManagerOwnRoute_succeeds() {
            // given
            UUID routeId = UUID.randomUUID();
            UUID managerId = UUID.randomUUID();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.WAITING_AT_HUB,
                    UUID.randomUUID(), UUID.randomUUID(), managerId);
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.IN_TRANSIT);

            // when & then
            var response = deliveryRouteService.update(routeId, request, managerId.toString(), UserRole.DELIVERY_MANAGER);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("DELIVERY_MANAGER가 타인 경로 수정 시 ROUTE_FORBIDDEN 예외 발생")
        void update_deliveryManagerOtherRoute_throwsForbidden() {
            // given
            UUID routeId = UUID.randomUUID();
            UUID assignedManagerId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.WAITING_AT_HUB,
                    UUID.randomUUID(), UUID.randomUUID(), assignedManagerId);
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.IN_TRANSIT);

            // when & then
            assertThatThrownBy(() ->
                    deliveryRouteService.update(routeId, request, otherUserId.toString(), UserRole.DELIVERY_MANAGER))
                    .isInstanceOf(DeliveryRouteException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryRouteErrorCode.ROUTE_FORBIDDEN);
        }

        @Test
        @DisplayName("COMPANY_MANAGER는 경로 수정 불가 - ROUTE_FORBIDDEN 예외 발생")
        void update_companyManagerRole_throwsForbidden() {
            // given
            UUID routeId = UUID.randomUUID();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.WAITING_AT_HUB,
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            DeliveryRouteUpdateRequest request = buildUpdateRequest(DeliveryRouteStatus.IN_TRANSIT);

            // when & then
            assertThatThrownBy(() ->
                    deliveryRouteService.update(routeId, request, UUID.randomUUID().toString(), UserRole.COMPANY_MANAGER))
                    .isInstanceOf(DeliveryRouteException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryRouteErrorCode.ROUTE_FORBIDDEN);
        }
    }

    // -----------------------------------------------------------------------
    // 단건 조회 테스트
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("단건 조회")
    class GetById {

        @Test
        @DisplayName("MASTER는 모든 경로 단건 조회 가능")
        void getById_masterRole_succeeds() {
            // given
            UUID routeId = UUID.randomUUID();
            DeliveryRoute route = buildRoute(routeId, DeliveryRouteStatus.IN_TRANSIT,
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.of(route));

            // when
            DeliveryRouteResponse response = deliveryRouteService.getById(
                    routeId, UUID.randomUUID().toString(), UserRole.MASTER);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(DeliveryRouteStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("존재하지 않는 경로 조회 시 ROUTE_NOT_FOUND 예외 발생")
        void getById_notFound_throwsException() {
            // given
            UUID routeId = UUID.randomUUID();
            given(deliveryRouteRepository.findByIdAndDeletedAtIsNull(routeId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    deliveryRouteService.getById(routeId, UUID.randomUUID().toString(), UserRole.MASTER))
                    .isInstanceOf(DeliveryRouteException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryRouteErrorCode.ROUTE_NOT_FOUND);
        }
    }
}
