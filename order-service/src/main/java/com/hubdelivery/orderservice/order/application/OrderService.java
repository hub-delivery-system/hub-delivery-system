package com.hubdelivery.orderservice.order.application;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.orderservice.order.domain.entity.Order;
import com.hubdelivery.orderservice.order.domain.exception.OrderErrorCode;
import com.hubdelivery.orderservice.order.domain.exception.OrderException;
import com.hubdelivery.orderservice.order.domain.repository.OrderRepository;
import com.hubdelivery.orderservice.order.infrastructure.client.delivery.DeliveryServiceClient;
import com.hubdelivery.orderservice.order.infrastructure.client.delivery.dto.DeliveryCreateRequest;
import com.hubdelivery.orderservice.order.infrastructure.client.delivery.dto.DeliveryCreateRequest.RouteRequest;
import com.hubdelivery.orderservice.order.infrastructure.client.product.ProductServiceClient;
import com.hubdelivery.orderservice.order.infrastructure.client.user.UserServiceClient;
import com.hubdelivery.orderservice.order.presentation.dto.OrderCreateRequest;
import com.hubdelivery.orderservice.order.presentation.dto.OrderResponse;
import com.hubdelivery.orderservice.order.presentation.dto.OrderSearchCondition;
import com.hubdelivery.orderservice.order.presentation.dto.OrderUpdateRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeliveryServiceClient deliveryServiceClient;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @Transactional
    public OrderResponse create(OrderCreateRequest request, String userId, UserRole role) {
        // 1. 주문 저장 (producerId는 현재 로그인한 사용자)
        Order order = Order.builder()
                .producerId(UUID.fromString(userId))
                .receiverId(request.getReceiverId())
                .productId(request.getProductId())
                .amount(request.getAmount())
                .requestMessage(request.getRequestMessage())
                .build();

        Order saved = orderRepository.save(order);

        // 2. delivery-service 연동하여 배송 생성
        try {
            var deliveryResp = deliveryServiceClient.create(buildDeliveryRequest(saved.getId(), request));
            saved.assignDelivery(deliveryResp.data().getId());
        } catch (Exception e) {
            throw new OrderException(OrderErrorCode.DELIVERY_CREATE_FAILED);
        }

        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAll(
            int page, int size,
            OrderSearchCondition cond,
            String userId, UserRole role) {

        Pageable pageable = PageableUtils.createPageable(page, size);

        // DELIVERY_MANAGER·COMPANY_MANAGER는 본인이 요청한 주문만 조회
        UUID fixedProducerId = null;
        if (role == UserRole.DELIVERY_MANAGER || role == UserRole.COMPANY_MANAGER) {
            fixedProducerId = UUID.fromString(userId);
        }

        return PageResponse.from(
                orderRepository.searchOrders(cond, fixedProducerId, pageable)
                        .map(OrderResponse::from));
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id, String userId, UserRole role) {
        Order order = findActive(id);
        checkReadPermission(role, userId, order);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse update(UUID id, OrderUpdateRequest request, String userId, UserRole role) {
        Order order = findActive(id);
        checkUpdatePermission(role, userId, order);

        if (!order.getStatus().canTransitionTo(request.getStatus())) {
            throw new OrderException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }

        order.update(request.getStatus(), request.getRequestMessage());
        return OrderResponse.from(order);
    }

    @Transactional
    public void delete(UUID id, String userId, UserRole role) {
        Order order = findActive(id);
        checkDeletePermission(role, userId, order);
        order.softDelete(userId);
    }

    // -----------------------------------------------------------------------
    // 헬퍼
    // -----------------------------------------------------------------------

    private Order findActive(UUID id) {
        return orderRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    /** OrderCreateRequest → DeliveryCreateRequest 변환 */
    private DeliveryCreateRequest buildDeliveryRequest(UUID orderId, OrderCreateRequest req) {
        List<RouteRequest> routes = req.getRoutes().stream()
                .map(r -> RouteRequest.builder()
                        .sequence(r.getSequence())
                        .startHubId(r.getStartHubId())
                        .endHubId(r.getEndHubId())
                        .estimatedDistance(r.getEstimatedDistance())
                        .estimatedDuration(r.getEstimatedDuration())
                        .deliveryManagerId(r.getDeliveryManagerId())
                        .build())
                .toList();

        return DeliveryCreateRequest.builder()
                .orderId(orderId)
                .startHubId(req.getStartHubId())
                .endHubId(req.getEndHubId())
                .address(req.getAddress())
                .userId(req.getDeliveryUserId())
                .slackId(req.getSlackId())
                .routes(routes)
                .build();
    }

    // -----------------------------------------------------------------------
    // 권한 체크
    // -----------------------------------------------------------------------

    // 조회: 전체 가능, DELIVERY_MANAGER·COMPANY_MANAGER는 본인 주문만
    private void checkReadPermission(UserRole role, String userId, Order order) {
        if (role == UserRole.MASTER || role == UserRole.HUB_MANAGER) return;
        if (!order.getProducerId().toString().equals(userId)) {
            throw new OrderException(OrderErrorCode.ORDER_FORBIDDEN);
        }
    }

    // 수정: MASTER, HUB_MANAGER(담당 허브)만 가능
    private void checkUpdatePermission(UserRole role, String userId, Order order) {
        if (role == UserRole.MASTER) return;
        if (role == UserRole.HUB_MANAGER) {
            checkHubPermission(userId, order);
            return;
        }
        throw new OrderException(OrderErrorCode.ORDER_FORBIDDEN);
    }

    // 삭제: MASTER, HUB_MANAGER(담당 허브)만 가능
    private void checkDeletePermission(UserRole role, String userId, Order order) {
        if (role == UserRole.MASTER) return;
        if (role == UserRole.HUB_MANAGER) {
            checkHubPermission(userId, order);
            return;
        }
        throw new OrderException(OrderErrorCode.ORDER_FORBIDDEN);
    }

    // HUB_MANAGER 허브 권한 검증: 상품의 hubId == 사용자의 hubId
    private void checkHubPermission(String userId, Order order) {
        UUID userHubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
        UUID productHubId = productServiceClient.getProduct(order.getProductId()).data().getHubId();
        if (!userHubId.equals(productHubId)) {
            throw new OrderException(OrderErrorCode.ORDER_FORBIDDEN);
        }
    }
}
