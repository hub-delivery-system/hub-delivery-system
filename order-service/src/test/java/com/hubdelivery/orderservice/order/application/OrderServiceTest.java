package com.hubdelivery.orderservice.order.application;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.orderservice.order.domain.entity.Order;
import com.hubdelivery.orderservice.order.domain.exception.OrderErrorCode;
import com.hubdelivery.orderservice.order.domain.exception.OrderException;
import com.hubdelivery.orderservice.order.domain.repository.OrderRepository;
import com.hubdelivery.orderservice.order.domain.type.OrderStatus;
import com.hubdelivery.orderservice.order.infrastructure.client.delivery.DeliveryServiceClient;
import com.hubdelivery.orderservice.order.infrastructure.client.delivery.dto.DeliveryResponse;
import com.hubdelivery.orderservice.order.infrastructure.client.product.ProductServiceClient;
import com.hubdelivery.orderservice.order.infrastructure.client.product.dto.ProductResponse;
import com.hubdelivery.orderservice.order.infrastructure.client.user.UserServiceClient;
import com.hubdelivery.orderservice.order.infrastructure.client.user.dto.UserResponse;
import com.hubdelivery.orderservice.order.presentation.dto.OrderCreateRequest;
import com.hubdelivery.orderservice.order.presentation.dto.OrderUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryServiceClient deliveryServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    // -----------------------------------------------------------------------
    // 테스트 헬퍼
    // -----------------------------------------------------------------------

    /** 리플렉션으로 Order의 id·status 필드를 강제 설정 */
    private Order buildOrder(UUID id, OrderStatus status, UUID producerId, UUID productId) {
        Order order = Order.builder()
                .producerId(producerId)
                .receiverId(UUID.randomUUID())
                .productId(productId)
                .amount(10)
                .requestMessage("빠른 납품 요청")
                .build();
        try {
            var idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);

            var statusField = Order.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(order, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return order;
    }

    /** OrderCreateRequest 생성 헬퍼 */
    private OrderCreateRequest buildCreateRequest() {
        try {
            var req = new OrderCreateRequest();
            setField(req, "receiverId", UUID.randomUUID());
            setField(req, "productId", UUID.randomUUID());
            setField(req, "amount", 5);
            setField(req, "startHubId", UUID.randomUUID());
            setField(req, "endHubId", UUID.randomUUID());
            setField(req, "address", "서울시 강남구");
            setField(req, "deliveryUserId", UUID.randomUUID());
            setField(req, "slackId", "U-SLACK-123");

            var routeReq = new OrderCreateRequest.RouteRequest();
            setField(routeReq, "sequence", 1);
            setField(routeReq, "startHubId", UUID.randomUUID());
            setField(routeReq, "endHubId", UUID.randomUUID());
            setField(req, "routes", List.of(routeReq));
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** OrderUpdateRequest 생성 헬퍼 */
    private OrderUpdateRequest buildUpdateRequest(OrderStatus status) {
        try {
            var req = new OrderUpdateRequest();
            setField(req, "status", status);
            return req;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String name, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    // -----------------------------------------------------------------------
    // 주문 생성 테스트
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("주문 생성 시 배송이 함께 생성되고 deliveryId가 연결된다")
        void create_success_withDeliveryLinked() {
            // given
            String userId = UUID.randomUUID().toString();
            UUID deliveryId = UUID.randomUUID();
            OrderCreateRequest request = buildCreateRequest();

            Order savedOrder = buildOrder(UUID.randomUUID(), OrderStatus.PENDING,
                    UUID.fromString(userId), request.getProductId());
            given(orderRepository.save(any())).willReturn(savedOrder);

            DeliveryResponse deliveryResponse = mock(DeliveryResponse.class);
            given(deliveryResponse.getId()).willReturn(deliveryId);
            given(deliveryServiceClient.create(any()))
                    .willReturn(ApiResponse.created(deliveryResponse));

            // when
            var response = orderService.create(request, userId, UserRole.COMPANY_MANAGER);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("배송 생성 실패 시 DELIVERY_CREATE_FAILED 예외 발생")
        void create_deliveryFailed_throwsException() {
            // given
            String userId = UUID.randomUUID().toString();
            OrderCreateRequest request = buildCreateRequest();

            Order savedOrder = buildOrder(UUID.randomUUID(), OrderStatus.PENDING,
                    UUID.fromString(userId), request.getProductId());
            given(orderRepository.save(any())).willReturn(savedOrder);
            given(deliveryServiceClient.create(any())).willThrow(new RuntimeException("연결 실패"));

            // when & then
            assertThatThrownBy(() -> orderService.create(request, userId, UserRole.COMPANY_MANAGER))
                    .isInstanceOf(OrderException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.DELIVERY_CREATE_FAILED);
        }
    }

    // -----------------------------------------------------------------------
    // 상태 전이 테스트
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("PENDING → CONFIRMED 전이 성공")
        void update_pendingToConfirmed_succeeds() {
            // given
            UUID orderId = UUID.randomUUID();
            Order order = buildOrder(orderId, OrderStatus.PENDING, UUID.randomUUID(), UUID.randomUUID());
            given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            // when
            var response = orderService.update(orderId, buildUpdateRequest(OrderStatus.CONFIRMED),
                    UUID.randomUUID().toString(), UserRole.MASTER);

            // then
            assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("COMPLETED 상태에서 다른 상태로 전이 시 INVALID_STATUS_TRANSITION 예외 발생")
        void update_fromCompleted_throwsInvalidTransition() {
            // given
            UUID orderId = UUID.randomUUID();
            Order order = buildOrder(orderId, OrderStatus.COMPLETED, UUID.randomUUID(), UUID.randomUUID());
            given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.update(orderId, buildUpdateRequest(OrderStatus.PENDING),
                    UUID.randomUUID().toString(), UserRole.MASTER))
                    .isInstanceOf(OrderException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("CANCELED 상태에서 다른 상태로 전이 시 INVALID_STATUS_TRANSITION 예외 발생")
        void update_fromCanceled_throwsInvalidTransition() {
            // given
            UUID orderId = UUID.randomUUID();
            Order order = buildOrder(orderId, OrderStatus.CANCELED, UUID.randomUUID(), UUID.randomUUID());
            given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.update(orderId, buildUpdateRequest(OrderStatus.CONFIRMED),
                    UUID.randomUUID().toString(), UserRole.MASTER))
                    .isInstanceOf(OrderException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("CONFIRMED → CANCELED 취소 전이 성공")
        void update_confirmedToCanceled_succeeds() {
            // given
            UUID orderId = UUID.randomUUID();
            Order order = buildOrder(orderId, OrderStatus.CONFIRMED, UUID.randomUUID(), UUID.randomUUID());
            given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            // when
            var response = orderService.update(orderId, buildUpdateRequest(OrderStatus.CANCELED),
                    UUID.randomUUID().toString(), UserRole.MASTER);

            // then
            assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }
    }

    // -----------------------------------------------------------------------
    // 권한 제어 테스트
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("권한 제어")
    class Authorization {

        @Test
        @DisplayName("HUB_MANAGER가 담당 허브 주문 수정 가능")
        void update_hubManagerOwnHub_succeeds() {
            // given
            UUID orderId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            String userId = UUID.randomUUID().toString();

            Order order = buildOrder(orderId, OrderStatus.PENDING, UUID.randomUUID(), productId);
            given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            UserResponse userResp = mock(UserResponse.class);
            given(userResp.getHubId()).willReturn(hubId);
            given(userServiceClient.getUser(any())).willReturn(ApiResponse.ok(userResp));

            ProductResponse productResp = mock(ProductResponse.class);
            given(productResp.getHubId()).willReturn(hubId); // 동일한 허브
            given(productServiceClient.getProduct(any())).willReturn(ApiResponse.ok(productResp));

            // when & then
            var response = orderService.update(orderId, buildUpdateRequest(OrderStatus.CONFIRMED),
                    userId, UserRole.HUB_MANAGER);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("HUB_MANAGER가 담당 허브가 아닌 주문 수정 시 ORDER_FORBIDDEN 예외 발생")
        void update_hubManagerOtherHub_throwsForbidden() {
            // given
            UUID orderId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            String userId = UUID.randomUUID().toString();

            Order order = buildOrder(orderId, OrderStatus.PENDING, UUID.randomUUID(), productId);
            given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            UserResponse userResp = mock(UserResponse.class);
            given(userResp.getHubId()).willReturn(UUID.randomUUID()); // 다른 허브
            given(userServiceClient.getUser(any())).willReturn(ApiResponse.ok(userResp));

            ProductResponse productResp = mock(ProductResponse.class);
            given(productResp.getHubId()).willReturn(UUID.randomUUID()); // 또 다른 허브
            given(productServiceClient.getProduct(any())).willReturn(ApiResponse.ok(productResp));

            // when & then
            assertThatThrownBy(() -> orderService.update(orderId, buildUpdateRequest(OrderStatus.CONFIRMED),
                    userId, UserRole.HUB_MANAGER))
                    .isInstanceOf(OrderException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_FORBIDDEN);
        }

        @Test
        @DisplayName("DELIVERY_MANAGER는 수정 불가 - ORDER_FORBIDDEN 예외 발생")
        void update_deliveryManager_throwsForbidden() {
            // given
            UUID orderId = UUID.randomUUID();
            Order order = buildOrder(orderId, OrderStatus.PENDING, UUID.randomUUID(), UUID.randomUUID());
            given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.update(orderId, buildUpdateRequest(OrderStatus.CONFIRMED),
                    UUID.randomUUID().toString(), UserRole.DELIVERY_MANAGER))
                    .isInstanceOf(OrderException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_FORBIDDEN);
        }

        @Test
        @DisplayName("DELIVERY_MANAGER는 본인 주문 단건 조회 가능")
        void getById_deliveryManagerOwnOrder_succeeds() {
            // given
            UUID orderId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Order order = buildOrder(orderId, OrderStatus.PENDING, userId, UUID.randomUUID());
            given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            // when & then
            var response = orderService.getById(orderId, userId.toString(), UserRole.DELIVERY_MANAGER);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("DELIVERY_MANAGER가 타인 주문 조회 시 ORDER_FORBIDDEN 예외 발생")
        void getById_deliveryManagerOtherOrder_throwsForbidden() {
            // given
            UUID orderId = UUID.randomUUID();
            UUID ownerUserId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            Order order = buildOrder(orderId, OrderStatus.PENDING, ownerUserId, UUID.randomUUID());
            given(orderRepository.findByIdAndDeletedAtIsNull(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() ->
                    orderService.getById(orderId, otherUserId.toString(), UserRole.DELIVERY_MANAGER))
                    .isInstanceOf(OrderException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_FORBIDDEN);
        }
    }
}
