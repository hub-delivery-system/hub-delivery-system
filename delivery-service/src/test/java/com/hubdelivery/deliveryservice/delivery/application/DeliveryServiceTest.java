package com.hubdelivery.deliveryservice.delivery.application;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.deliveryservice.delivery.domain.entity.Delivery;
import com.hubdelivery.deliveryservice.delivery.domain.exception.DeliveryErrorCode;
import com.hubdelivery.deliveryservice.delivery.domain.exception.DeliveryException;
import com.hubdelivery.deliveryservice.delivery.domain.repository.DeliveryRepository;
import com.hubdelivery.deliveryservice.delivery.domain.type.DeliveryStatus;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliveryCreateRequest;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliveryUpdateRequest;
import com.hubdelivery.deliveryservice.deliverymanager.application.DeliveryManagerService;
import com.hubdelivery.deliveryservice.deliverymanager.domain.entity.DeliveryManager;
import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.UserServiceClient;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.dto.UserResponse;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerResponse;
import com.hubdelivery.deliveryservice.deliveryroute.domain.entity.DeliveryRoute;
import com.hubdelivery.deliveryservice.deliveryroute.domain.repository.DeliveryRouteRepository;
import com.hubdelivery.deliveryservice.deliveryroute.domain.type.DeliveryRouteStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @InjectMocks
    private DeliveryService deliveryService;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryRouteRepository deliveryRouteRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private DeliveryManagerService deliveryManagerService;

    // -----------------------------------------------------------------------
    // 테스트 헬퍼
    // -----------------------------------------------------------------------

    private Delivery buildDelivery(UUID id, DeliveryStatus status, UUID startHubId, UUID endHubId, UUID managerId) {
        Delivery d = Delivery.builder()
                .orderId(UUID.randomUUID())
                .status(status)
                .startHubId(startHubId)
                .endHubId(endHubId)
                .address("서울시 강남구")
                .userId(UUID.randomUUID())
                .slackId("slack-id")
                .deliveryManagerId(managerId)
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

    private DeliveryManagerResponse buildManagerResponse(UUID managerId, int sequence) {
        DeliveryManager manager = DeliveryManager.builder()
                .userId(UUID.randomUUID())
                .type(DeliveryManagerType.COMPANY_DELIVERY)
                .sequence(sequence)
                .build();
        try {
            var idField = DeliveryManager.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(manager, managerId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return DeliveryManagerResponse.from(manager);
    }

    private DeliveryCreateRequest buildCreateRequest(UUID endHubId) {
        try {
            var request = new DeliveryCreateRequest();
            setField(request, "orderId", UUID.randomUUID());
            setField(request, "startHubId", UUID.randomUUID());
            setField(request, "endHubId", endHubId);
            setField(request, "address", "서울시 강남구");
            setField(request, "userId", UUID.randomUUID());
            setField(request, "slackId", "slack-test");

            var routeReq = new DeliveryCreateRequest.RouteRequest();
            setField(routeReq, "sequence", 1);
            setField(routeReq, "startHubId", UUID.randomUUID());
            setField(routeReq, "endHubId", endHubId);
            setField(routeReq, "estimatedDistance", BigDecimal.valueOf(100.0));
            setField(routeReq, "estimatedDuration", 60);
            setField(request, "routes", List.of(routeReq));

            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DeliveryUpdateRequest buildUpdateRequest(DeliveryStatus status) {
        try {
            var request = new DeliveryUpdateRequest();
            setField(request, "status", status);
            setField(request, "address", "서울시 강남구");
            setField(request, "userId", UUID.randomUUID());
            setField(request, "slackId", "slack-test");
            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // -----------------------------------------------------------------------
    // 배송 생성 테스트
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("배송 생성")
    class CreateDelivery {

        @Test
        @DisplayName("MASTER가 배송 생성 시 COMPANY_DELIVERY 담당자가 순환 배정된다")
        void create_masterRole_assignsCompanyDeliveryManager() {
            // given
            UUID endHubId = UUID.randomUUID();
            UUID assignedManagerId = UUID.randomUUID();
            DeliveryCreateRequest request = buildCreateRequest(endHubId);

            // 이전 배정 이력 없음 (첫 번째 배정)
            given(deliveryRepository.findLatestDeliveryManagerIdByEndHub(any(), any()))
                    .willReturn(new PageImpl<>(Collections.emptyList()));
            given(deliveryManagerService.getSequenceById(any())).willReturn(0);
            given(deliveryManagerService.assignNext(any(), eq(DeliveryManagerType.COMPANY_DELIVERY), anyInt()))
                    .willReturn(buildManagerResponse(assignedManagerId, 1));

            Delivery savedDelivery = buildDelivery(UUID.randomUUID(), DeliveryStatus.HUB_PENDING,
                    request.getStartHubId(), endHubId, assignedManagerId);
            given(deliveryRepository.save(any())).willReturn(savedDelivery);

            // HUB_DELIVERY 담당자 배정 (경로용)
            UUID hubManagerId = UUID.randomUUID();
            given(deliveryRouteRepository.findLatestHubDeliveryManagerId(any()))
                    .willReturn(new PageImpl<>(Collections.emptyList()));
            given(deliveryManagerService.assignNextByType(eq(DeliveryManagerType.HUB_DELIVERY), anyInt()))
                    .willReturn(buildManagerResponse(hubManagerId, 1));
            given(deliveryManagerService.getSequenceById(hubManagerId)).willReturn(1);

            DeliveryRoute savedRoute = DeliveryRoute.builder()
                    .deliveryId(savedDelivery.getId())
                    .sequence(1)
                    .startHubId(UUID.randomUUID())
                    .endHubId(endHubId)
                    .status(DeliveryRouteStatus.WAITING_AT_HUB)
                    .deliveryManagerId(hubManagerId)
                    .build();
            given(deliveryRouteRepository.saveAll(any())).willReturn(List.of(savedRoute));

            // when & then — 예외 없이 정상 실행되는지 확인
            var response = deliveryService.create(request, UUID.randomUUID().toString(), UserRole.MASTER);
            assertThat(response).isNotNull();
            assertThat(response.getDeliveryManagerId()).isEqualTo(assignedManagerId);
        }

        @Test
        @DisplayName("요청에 deliveryManagerId가 명시된 경우 해당 담당자를 그대로 사용한다")
        void create_withExplicitManagerId_usesDirectly() throws Exception {
            // given
            UUID endHubId = UUID.randomUUID();
            UUID explicitManagerId = UUID.randomUUID();
            DeliveryCreateRequest request = buildCreateRequest(endHubId);
            setField(request, "deliveryManagerId", explicitManagerId);

            Delivery savedDelivery = buildDelivery(UUID.randomUUID(), DeliveryStatus.HUB_PENDING,
                    request.getStartHubId(), endHubId, explicitManagerId);
            given(deliveryRepository.save(any())).willReturn(savedDelivery);

            UUID hubManagerId = UUID.randomUUID();
            given(deliveryRouteRepository.findLatestHubDeliveryManagerId(any()))
                    .willReturn(new PageImpl<>(Collections.emptyList()));
            given(deliveryManagerService.assignNextByType(eq(DeliveryManagerType.HUB_DELIVERY), anyInt()))
                    .willReturn(buildManagerResponse(hubManagerId, 1));
            given(deliveryManagerService.getSequenceById(hubManagerId)).willReturn(1);

            DeliveryRoute savedRoute = DeliveryRoute.builder()
                    .deliveryId(savedDelivery.getId())
                    .sequence(1)
                    .startHubId(UUID.randomUUID())
                    .endHubId(endHubId)
                    .status(DeliveryRouteStatus.WAITING_AT_HUB)
                    .deliveryManagerId(hubManagerId)
                    .build();
            given(deliveryRouteRepository.saveAll(any())).willReturn(List.of(savedRoute));

            // when
            var response = deliveryService.create(request, UUID.randomUUID().toString(), UserRole.MASTER);

            // then
            assertThat(response.getDeliveryManagerId()).isEqualTo(explicitManagerId);
        }

        @Test
        @DisplayName("MASTER 외 역할로 배송 생성 시 DELIVERY_FORBIDDEN 예외 발생")
        void create_nonMasterRole_throwsForbidden() {
            DeliveryCreateRequest request = buildCreateRequest(UUID.randomUUID());

            assertThatThrownBy(() ->
                    deliveryService.create(request, UUID.randomUUID().toString(), UserRole.HUB_MANAGER))
                    .isInstanceOf(DeliveryException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }
    }

    // -----------------------------------------------------------------------
    // 상태 전이 테스트
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("HUB_PENDING → HUB_IN_TRANSIT 전이 성공")
        void update_hubPendingToHubInTransit_succeeds() {
            // given
            UUID deliveryId = UUID.randomUUID();
            Delivery delivery = buildDelivery(deliveryId, DeliveryStatus.HUB_PENDING,
                    UUID.randomUUID(), UUID.randomUUID(), null);
            given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                    .willReturn(Optional.of(delivery));

            DeliveryUpdateRequest request = buildUpdateRequest(DeliveryStatus.HUB_IN_TRANSIT);

            // when & then — 예외 없이 정상 실행
            var response = deliveryService.update(deliveryId, request, UUID.randomUUID().toString(), UserRole.MASTER);
            assertThat(response.getStatus()).isEqualTo(DeliveryStatus.HUB_IN_TRANSIT);
        }

        @Test
        @DisplayName("DELIVERING → HUB_PENDING 역방향 전이 시 INVALID_STATUS_TRANSITION 예외 발생")
        void update_invalidTransition_throwsException() {
            // given
            UUID deliveryId = UUID.randomUUID();
            Delivery delivery = buildDelivery(deliveryId, DeliveryStatus.DELIVERING,
                    UUID.randomUUID(), UUID.randomUUID(), null);
            given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                    .willReturn(Optional.of(delivery));

            DeliveryUpdateRequest request = buildUpdateRequest(DeliveryStatus.HUB_PENDING);

            // when & then
            assertThatThrownBy(() ->
                    deliveryService.update(deliveryId, request, UUID.randomUUID().toString(), UserRole.MASTER))
                    .isInstanceOf(DeliveryException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryErrorCode.INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("DELIVERED 상태에서 다른 상태로 전이 불가")
        void update_fromDelivered_throwsInvalidTransition() {
            // given
            UUID deliveryId = UUID.randomUUID();
            Delivery delivery = buildDelivery(deliveryId, DeliveryStatus.DELIVERED,
                    UUID.randomUUID(), UUID.randomUUID(), null);
            given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                    .willReturn(Optional.of(delivery));

            DeliveryUpdateRequest request = buildUpdateRequest(DeliveryStatus.DELIVERING);

            // when & then
            assertThatThrownBy(() ->
                    deliveryService.update(deliveryId, request, UUID.randomUUID().toString(), UserRole.MASTER))
                    .isInstanceOf(DeliveryException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryErrorCode.INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("DELIVERING 에서 DELIVERED 와 PARTNER_TRANSFER 두 가지 전이 모두 성공")
        void update_deliveringToDelivered_succeeds() {
            // given
            UUID deliveryId = UUID.randomUUID();
            Delivery delivery = buildDelivery(deliveryId, DeliveryStatus.DELIVERING,
                    UUID.randomUUID(), UUID.randomUUID(), null);
            given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                    .willReturn(Optional.of(delivery));

            DeliveryUpdateRequest request = buildUpdateRequest(DeliveryStatus.DELIVERED);

            // when
            var response = deliveryService.update(deliveryId, request, UUID.randomUUID().toString(), UserRole.MASTER);

            // then
            assertThat(response.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        }
    }

    // -----------------------------------------------------------------------
    // 권한 제어 테스트
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("권한 제어")
    class Authorization {

        @Test
        @DisplayName("HUB_MANAGER는 담당 허브(startHub 또는 endHub) 배송 수정 가능")
        void update_hubManagerOwnHub_succeeds() {
            // given
            UUID deliveryId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            String userId = UUID.randomUUID().toString();
            Delivery delivery = buildDelivery(deliveryId, DeliveryStatus.HUB_PENDING,
                    hubId, UUID.randomUUID(), null);
            given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                    .willReturn(Optional.of(delivery));

            UserResponse userResponse = mock(UserResponse.class);
            given(userResponse.getHubId()).willReturn(hubId);
            given(userServiceClient.getUser(any())).willReturn(ApiResponse.ok(userResponse));

            DeliveryUpdateRequest request = buildUpdateRequest(DeliveryStatus.HUB_IN_TRANSIT);

            // when & then — 예외 없이 수정 가능
            var response = deliveryService.update(deliveryId, request, userId, UserRole.HUB_MANAGER);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("HUB_MANAGER가 담당 허브가 아닌 배송 수정 시 DELIVERY_FORBIDDEN 예외 발생")
        void update_hubManagerOtherHub_throwsForbidden() {
            // given
            UUID deliveryId = UUID.randomUUID();
            UUID managerHubId = UUID.randomUUID();
            UUID otherHubId = UUID.randomUUID();
            String userId = UUID.randomUUID().toString();
            Delivery delivery = buildDelivery(deliveryId, DeliveryStatus.HUB_PENDING,
                    otherHubId, UUID.randomUUID(), null); // startHub, endHub 모두 다른 허브
            given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                    .willReturn(Optional.of(delivery));

            UserResponse userResponse = mock(UserResponse.class);
            given(userResponse.getHubId()).willReturn(managerHubId);
            given(userServiceClient.getUser(any())).willReturn(ApiResponse.ok(userResponse));

            DeliveryUpdateRequest request = buildUpdateRequest(DeliveryStatus.HUB_IN_TRANSIT);

            // when & then
            assertThatThrownBy(() ->
                    deliveryService.update(deliveryId, request, userId, UserRole.HUB_MANAGER))
                    .isInstanceOf(DeliveryException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }

        @Test
        @DisplayName("DELIVERY_MANAGER는 본인이 담당하는 배송 수정 가능")
        void update_deliveryManagerOwnDelivery_succeeds() {
            // given
            UUID deliveryId = UUID.randomUUID();
            UUID managerId = UUID.randomUUID();
            Delivery delivery = buildDelivery(deliveryId, DeliveryStatus.HUB_PENDING,
                    UUID.randomUUID(), UUID.randomUUID(), managerId);
            given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                    .willReturn(Optional.of(delivery));

            DeliveryUpdateRequest request = buildUpdateRequest(DeliveryStatus.HUB_IN_TRANSIT);

            // when & then
            var response = deliveryService.update(deliveryId, request, managerId.toString(), UserRole.DELIVERY_MANAGER);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("DELIVERY_MANAGER가 타인 배송 수정 시 DELIVERY_FORBIDDEN 예외 발생")
        void update_deliveryManagerOtherDelivery_throwsForbidden() {
            // given
            UUID deliveryId = UUID.randomUUID();
            UUID assignedManagerId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID(); // 배정된 담당자가 아님
            Delivery delivery = buildDelivery(deliveryId, DeliveryStatus.HUB_PENDING,
                    UUID.randomUUID(), UUID.randomUUID(), assignedManagerId);
            given(deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId))
                    .willReturn(Optional.of(delivery));

            DeliveryUpdateRequest request = buildUpdateRequest(DeliveryStatus.HUB_IN_TRANSIT);

            // when & then
            assertThatThrownBy(() ->
                    deliveryService.update(deliveryId, request, otherUserId.toString(), UserRole.DELIVERY_MANAGER))
                    .isInstanceOf(DeliveryException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }
    }
}
