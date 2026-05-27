package com.hubdelivery.deliveryservice.delivery.application;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.deliveryservice.delivery.domain.entity.Delivery;
import com.hubdelivery.deliveryservice.delivery.domain.exception.DeliveryErrorCode;
import com.hubdelivery.deliveryservice.delivery.domain.exception.DeliveryException;
import com.hubdelivery.deliveryservice.delivery.domain.repository.DeliveryRepository;
import com.hubdelivery.deliveryservice.delivery.domain.type.DeliveryStatus;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliveryCreateRequest;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliveryCreateRequest.RouteRequest;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliveryResponse;
import com.hubdelivery.deliveryservice.delivery.presentation.dto.DeliveryUpdateRequest;
import com.hubdelivery.deliveryservice.deliverymanager.application.DeliveryManagerService;
import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import com.hubdelivery.deliveryservice.deliveryroute.domain.entity.DeliveryRoute;
import com.hubdelivery.deliveryservice.deliveryroute.domain.repository.DeliveryRouteRepository;
import com.hubdelivery.deliveryservice.deliveryroute.domain.type.DeliveryRouteStatus;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteResponse;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.UserServiceClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteRepository deliveryRouteRepository;
    private final UserServiceClient userServiceClient;
    private final DeliveryManagerService deliveryManagerService;

    @Transactional
    public DeliveryResponse create(DeliveryCreateRequest request, String userId, UserRole role) {
        if (role != UserRole.MASTER) {
            throw new DeliveryException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
        }

        // 1. 목적지 허브 소속 COMPANY_DELIVERY_MANAGER 순환 배정
        UUID assignedManagerId = resolveCompanyDeliveryManager(request);

        Delivery delivery = Delivery.builder()
                .orderId(request.getOrderId())
                .status(DeliveryStatus.HUB_PENDING)
                .startHubId(request.getStartHubId())
                .endHubId(request.getEndHubId())
                .address(request.getAddress())
                .userId(request.getUserId())
                .slackId(request.getSlackId())
                .deliveryManagerId(assignedManagerId)
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        // 2. 각 경로에 HUB_DELIVERY_MANAGER 순환 배정 후 일괄 생성 (단일 트랜잭션)
        List<DeliveryRoute> routes = buildRoutes(saved.getId(), request.getRoutes());
        List<DeliveryRoute> savedRoutes = deliveryRouteRepository.saveAll(routes);

        List<DeliveryRouteResponse> routeResponses = savedRoutes.stream()
                .map(DeliveryRouteResponse::from)
                .toList();

        return DeliveryResponse.withRoutes(saved, routeResponses);
    }

    @Transactional(readOnly = true)
    public PageResponse<DeliveryResponse> getAll(int page, int size, String userId, UserRole role) {
        Pageable pageable = PageableUtils.createPageable(page, size);

        if (role == UserRole.HUB_MANAGER) {
            UUID hubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
            return PageResponse.from(
                    deliveryRepository.findAllByHubIdAndDeletedAtIsNull(hubId, pageable)
                            .map(DeliveryResponse::from));
        }

        if (role == UserRole.DELIVERY_MANAGER) {
            return PageResponse.from(
                    deliveryRepository.findAllByDeliveryManagerIdAndDeletedAtIsNull(
                            UUID.fromString(userId), pageable)
                            .map(DeliveryResponse::from));
        }

        // MASTER, COMPANY_MANAGER: 전체 조회
        return PageResponse.from(
                deliveryRepository.findAllByDeletedAtIsNull(pageable)
                        .map(DeliveryResponse::from));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getById(UUID id, String userId, UserRole role) {
        Delivery delivery = findActive(id);
        checkReadPermission(role, userId, delivery);

        List<DeliveryRouteResponse> routes =
                deliveryRouteRepository.findAllByDeliveryIdAndDeletedAtIsNullOrderBySequenceAsc(id)
                        .stream()
                        .map(DeliveryRouteResponse::from)
                        .toList();

        return DeliveryResponse.withRoutes(delivery, routes);
    }

    @Transactional
    public DeliveryResponse update(UUID id, DeliveryUpdateRequest request, String userId, UserRole role) {
        Delivery delivery = findActive(id);
        checkUpdatePermission(role, userId, delivery);

        // 상태 전이 검증: 현재 상태에서 요청 상태로의 전이가 허용되는지 확인
        if (!delivery.getStatus().canTransitionTo(request.getStatus())) {
            throw new DeliveryException(DeliveryErrorCode.INVALID_STATUS_TRANSITION);
        }

        delivery.update(
                request.getStatus(),
                request.getAddress(),
                request.getUserId(),
                request.getSlackId(),
                request.getDeliveryManagerId());

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public void delete(UUID id, String userId, UserRole role) {
        Delivery delivery = findActive(id);
        checkDeletePermission(role, userId, delivery);
        delivery.softDelete(userId);
    }

    // -----------------------------------------------------------------------
    // 순환 배정 헬퍼
    // -----------------------------------------------------------------------

    /**
     * 요청에 deliveryManagerId가 지정된 경우 그대로 사용,
     * 없으면 endHubId 기준 COMPANY_DELIVERY_MANAGER 순환 배정
     */
    private UUID resolveCompanyDeliveryManager(DeliveryCreateRequest request) {
        if (request.getDeliveryManagerId() != null) {
            return request.getDeliveryManagerId();
        }

        // endHubId에 마지막으로 배정된 담당자의 sequence를 currentSequence로 사용
        int lastSeq = deliveryRepository
                .findLatestDeliveryManagerIdByEndHub(request.getEndHubId(), PageRequest.of(0, 1))
                .getContent().stream().findFirst()
                .map(deliveryManagerService::getSequenceById)
                .orElse(0);

        return deliveryManagerService.assignNext(
                request.getEndHubId(), DeliveryManagerType.COMPANY_DELIVERY, lastSeq).getId();
    }

    /**
     * 각 경로에 HUB_DELIVERY_MANAGER 순환 배정.
     * 한 번의 create 요청 내에서 여러 경로에 순차적으로 다른 담당자를 배정한다.
     */
    private List<DeliveryRoute> buildRoutes(UUID deliveryId, List<RouteRequest> routeRequests) {
        List<DeliveryRoute> routes = new ArrayList<>();
        int lastHubSeq = 0;

        for (RouteRequest r : routeRequests) {
            UUID routeManagerId;
            if (r.getDeliveryManagerId() != null) {
                routeManagerId = r.getDeliveryManagerId(); // 수동 지정 우선
            } else {
                routeManagerId = deliveryManagerService
                        .assignNextByType(DeliveryManagerType.HUB_DELIVERY, lastHubSeq).getId();
            }
            lastHubSeq = deliveryManagerService.getSequenceById(routeManagerId);

            routes.add(DeliveryRoute.builder()
                    .deliveryId(deliveryId)
                    .sequence(r.getSequence())
                    .startHubId(r.getStartHubId())
                    .endHubId(r.getEndHubId())
                    .estimatedDistance(r.getEstimatedDistance())
                    .estimatedDuration(r.getEstimatedDuration())
                    .status(DeliveryRouteStatus.WAITING_AT_HUB)
                    .deliveryManagerId(routeManagerId)
                    .build());
        }

        return routes;
    }

    // -----------------------------------------------------------------------
    // 권한 체크
    // -----------------------------------------------------------------------

    private Delivery findActive(UUID id) {
        return deliveryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
    }

    // 조회: MASTER, HUB_MANAGER(담당 허브), DELIVERY_MANAGER(본인), COMPANY_MANAGER
    private void checkReadPermission(UserRole role, String userId, Delivery delivery) {
        if (role == UserRole.MASTER || role == UserRole.COMPANY_MANAGER) return;
        if (role == UserRole.HUB_MANAGER) {
            UUID hubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
            if (!hubId.equals(delivery.getStartHubId()) && !hubId.equals(delivery.getEndHubId())) {
                throw new DeliveryException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
            }
            return;
        }
        if (role == UserRole.DELIVERY_MANAGER
                && delivery.getDeliveryManagerId() != null
                && delivery.getDeliveryManagerId().toString().equals(userId)) return;
        throw new DeliveryException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }

    // 수정: MASTER, HUB_MANAGER(담당 허브), DELIVERY_MANAGER(본인)
    private void checkUpdatePermission(UserRole role, String userId, Delivery delivery) {
        if (role == UserRole.MASTER) return;
        if (role == UserRole.HUB_MANAGER) {
            UUID hubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
            if (!hubId.equals(delivery.getStartHubId()) && !hubId.equals(delivery.getEndHubId())) {
                throw new DeliveryException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
            }
            return;
        }
        if (role == UserRole.DELIVERY_MANAGER
                && delivery.getDeliveryManagerId() != null
                && delivery.getDeliveryManagerId().toString().equals(userId)) return;
        throw new DeliveryException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }

    // 삭제: MASTER, HUB_MANAGER(담당 허브)
    private void checkDeletePermission(UserRole role, String userId, Delivery delivery) {
        if (role == UserRole.MASTER) return;
        if (role == UserRole.HUB_MANAGER) {
            UUID hubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
            if (!hubId.equals(delivery.getStartHubId()) && !hubId.equals(delivery.getEndHubId())) {
                throw new DeliveryException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
            }
            return;
        }
        throw new DeliveryException(DeliveryErrorCode.DELIVERY_FORBIDDEN);
    }
}
