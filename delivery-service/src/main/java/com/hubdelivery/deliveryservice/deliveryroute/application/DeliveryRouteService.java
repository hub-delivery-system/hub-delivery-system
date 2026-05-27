package com.hubdelivery.deliveryservice.deliveryroute.application;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.deliveryservice.delivery.domain.repository.DeliveryRepository;
import com.hubdelivery.deliveryservice.deliveryroute.domain.entity.DeliveryRoute;
import com.hubdelivery.deliveryservice.deliveryroute.domain.exception.DeliveryRouteErrorCode;
import com.hubdelivery.deliveryservice.deliveryroute.domain.exception.DeliveryRouteException;
import com.hubdelivery.deliveryservice.deliveryroute.domain.repository.DeliveryRouteRepository;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteResponse;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteUpdateRequest;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.UserServiceClient;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryRouteService {

    private final DeliveryRouteRepository deliveryRouteRepository;
    private final DeliveryRepository deliveryRepository;
    private final UserServiceClient userServiceClient;

    @Transactional(readOnly = true)
    public PageResponse<DeliveryRouteResponse> getByDeliveryId(UUID deliveryId, int page, int size,
                                                                String userId, UserRole role) {
        validateDeliveryExists(deliveryId);
        Pageable pageable = PageableUtils.createPageable(page, size);

        if (role == UserRole.HUB_MANAGER) {
            UUID hubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
            return PageResponse.from(
                    deliveryRouteRepository.findAllByDeliveryIdAndHubIdAndDeletedAtIsNull(deliveryId, hubId, pageable)
                            .map(DeliveryRouteResponse::from));
        }

        if (role == UserRole.DELIVERY_MANAGER) {
            return PageResponse.from(
                    deliveryRouteRepository.findAllByDeliveryIdAndDeliveryManagerIdAndDeletedAtIsNull(
                            deliveryId, UUID.fromString(userId), pageable)
                            .map(DeliveryRouteResponse::from));
        }

        // MASTER, COMPANY_MANAGER: 해당 배송의 모든 경로 조회
        return PageResponse.from(
                deliveryRouteRepository.findAllByDeliveryIdAndDeletedAtIsNull(deliveryId, pageable)
                        .map(DeliveryRouteResponse::from));
    }

    @Transactional(readOnly = true)
    public DeliveryRouteResponse getById(UUID id, String userId, UserRole role) {
        DeliveryRoute route = findActive(id);
        checkReadPermission(role, userId, route);
        return DeliveryRouteResponse.from(route);
    }

    @Transactional
    public DeliveryRouteResponse update(UUID id, DeliveryRouteUpdateRequest request, String userId, UserRole role) {
        DeliveryRoute route = findActive(id);
        checkUpdatePermission(role, userId, route);

        // 상태 전이 검증: 현재 상태에서 요청 상태로의 전이가 허용되는지 확인
        if (request.getStatus() != null && !route.getStatus().canTransitionTo(request.getStatus())) {
            throw new DeliveryRouteException(DeliveryRouteErrorCode.INVALID_STATUS_TRANSITION);
        }

        route.update(
                request.getStatus(),
                request.getRealDistance(),
                request.getRealDuration(),
                request.getDeliveryManagerId());

        return DeliveryRouteResponse.from(route);
    }

    @Transactional
    public void delete(UUID id, String userId, UserRole role) {
        DeliveryRoute route = findActive(id);
        checkDeletePermission(role, userId, route);
        route.softDelete(userId);
    }

    private DeliveryRoute findActive(UUID id) {
        return deliveryRouteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DeliveryRouteException(DeliveryRouteErrorCode.ROUTE_NOT_FOUND));
    }

    private void validateDeliveryExists(UUID deliveryId) {
        deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new DeliveryRouteException(DeliveryRouteErrorCode.DELIVERY_NOT_FOUND));
    }

    // 조회: MASTER, HUB_MANAGER(담당 허브), DELIVERY_MANAGER(본인), COMPANY_MANAGER
    private void checkReadPermission(UserRole role, String userId, DeliveryRoute route) {
        if (role == UserRole.MASTER || role == UserRole.COMPANY_MANAGER) return;
        if (role == UserRole.HUB_MANAGER) {
            UUID hubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
            if (!hubId.equals(route.getStartHubId()) && !hubId.equals(route.getEndHubId())) {
                throw new DeliveryRouteException(DeliveryRouteErrorCode.ROUTE_FORBIDDEN);
            }
            return;
        }
        if (role == UserRole.DELIVERY_MANAGER
                && route.getDeliveryManagerId() != null
                && route.getDeliveryManagerId().toString().equals(userId)) return;
        throw new DeliveryRouteException(DeliveryRouteErrorCode.ROUTE_FORBIDDEN);
    }

    // 수정: MASTER, HUB_MANAGER(담당 허브), DELIVERY_MANAGER(본인)
    private void checkUpdatePermission(UserRole role, String userId, DeliveryRoute route) {
        if (role == UserRole.MASTER) return;
        if (role == UserRole.HUB_MANAGER) {
            UUID hubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
            if (!hubId.equals(route.getStartHubId()) && !hubId.equals(route.getEndHubId())) {
                throw new DeliveryRouteException(DeliveryRouteErrorCode.ROUTE_FORBIDDEN);
            }
            return;
        }
        if (role == UserRole.DELIVERY_MANAGER
                && route.getDeliveryManagerId() != null
                && route.getDeliveryManagerId().toString().equals(userId)) return;
        throw new DeliveryRouteException(DeliveryRouteErrorCode.ROUTE_FORBIDDEN);
    }

    // 삭제: MASTER, HUB_MANAGER(담당 허브)
    private void checkDeletePermission(UserRole role, String userId, DeliveryRoute route) {
        if (role == UserRole.MASTER) return;
        if (role == UserRole.HUB_MANAGER) {
            UUID hubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
            if (!hubId.equals(route.getStartHubId()) && !hubId.equals(route.getEndHubId())) {
                throw new DeliveryRouteException(DeliveryRouteErrorCode.ROUTE_FORBIDDEN);
            }
            return;
        }
        throw new DeliveryRouteException(DeliveryRouteErrorCode.ROUTE_FORBIDDEN);
    }
}
