package com.hubdelivery.deliveryservice.deliverymanager.application;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.deliveryservice.deliverymanager.domain.entity.DeliveryManager;
import com.hubdelivery.deliveryservice.deliverymanager.domain.exception.DeliveryManagerErrorCode;
import com.hubdelivery.deliveryservice.deliverymanager.domain.exception.DeliveryManagerException;
import com.hubdelivery.deliveryservice.deliverymanager.domain.repository.DeliveryManagerRepository;
import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.HubServiceClient;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.UserServiceClient;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerCreateRequest;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerResponse;
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerUpdateRequest;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryManagerService {

    private final DeliveryManagerRepository deliveryManagerRepository;
    private final HubServiceClient hubServiceClient;
    private final UserServiceClient userServiceClient;

    @Transactional
    public DeliveryManagerResponse create(DeliveryManagerCreateRequest request, String userId, UserRole role) {
        checkWritePermission(role, userId, request.getHubId());
        validateHubExists(request.getHubId());

        // 허브+타입 기준 마지막 순번 + 1로 자동 배정
        int sequence = deliveryManagerRepository.findMaxSequence(request.getHubId(), request.getType()) + 1;

        DeliveryManager manager = DeliveryManager.builder()
                .userId(request.getUserId())
                .hubId(request.getHubId())
                .companyId(request.getCompanyId())
                .type(request.getType())
                .sequence(sequence)
                .build();

        return DeliveryManagerResponse.from(deliveryManagerRepository.save(manager));
    }

    @Transactional(readOnly = true)
    public DeliveryManagerResponse getById(UUID id, String userId, UserRole role) {
        DeliveryManager manager = findActiveManager(id);
        checkReadPermission(role, userId, manager);
        return DeliveryManagerResponse.from(manager);
    }

    @Transactional(readOnly = true)
    public PageResponse<DeliveryManagerResponse> getAll(int page, int size, String userId, UserRole role) {
        checkWritePermission(role, userId, null);
        Pageable pageable = PageableUtils.createPageable(page, size);

        // HUB_MANAGER는 담당 허브 소속 담당자만 조회
        if (role == UserRole.HUB_MANAGER) {
            UUID hubId = userServiceClient.getUser(UUID.fromString(userId)).getData().getHubId();
            return PageResponse.from(
                    deliveryManagerRepository.findAllByHubIdAndDeletedAtIsNull(hubId, pageable)
                            .map(DeliveryManagerResponse::from)
            );
        }

        return PageResponse.from(
                deliveryManagerRepository.findAllByDeletedAtIsNull(pageable)
                        .map(DeliveryManagerResponse::from)
        );
    }

    @Transactional
    public DeliveryManagerResponse update(UUID id, DeliveryManagerUpdateRequest request, String userId, UserRole role) {
        DeliveryManager manager = findActiveManager(id);
        checkWritePermission(role, userId, manager.getHubId());

        validateHubExists(request.getHubId());

        // hubId가 변경된 경우 새 허브 기준으로 순번 재배정
        boolean hubChanged = !manager.getHubId().equals(request.getHubId());
        int sequence = hubChanged
                ? deliveryManagerRepository.findMaxSequence(request.getHubId(), request.getType()) + 1
                : manager.getSequence();

        manager.update(request.getHubId(), request.getCompanyId(), request.getType(), sequence);
        return DeliveryManagerResponse.from(manager);
    }

    @Transactional
    public void delete(UUID id, String userId, UserRole role) {
        DeliveryManager manager = findActiveManager(id);
        checkWritePermission(role, userId, manager.getHubId());
        manager.softDelete(userId);
    }

    /**
     * 순환 배정: 현재 순번 이후 담당자 조회 → 없으면 처음으로 wrap-around
     */
    @Transactional(readOnly = true)
    public DeliveryManagerResponse assignNext(UUID hubId, DeliveryManagerType type, int currentSequence) {
        Pageable first = PageRequest.of(0, 1);

        return deliveryManagerRepository.findNextAfter(hubId, type, currentSequence, first)
                .getContent()
                .stream()
                .findFirst()
                .or(() -> deliveryManagerRepository.findFirstByHubIdAndType(hubId, type, first)
                        .getContent()
                        .stream()
                        .findFirst())
                .map(DeliveryManagerResponse::from)
                .orElseThrow(() -> new DeliveryManagerException(DeliveryManagerErrorCode.NO_AVAILABLE_MANAGER));
    }

    // 생성·수정·삭제·목록 조회: MASTER 또는 HUB_MANAGER(담당 허브)만 허용
    private void checkWritePermission(UserRole role, String userId, UUID resourceHubId) {
        if (role == UserRole.MASTER) return;
        if (role == UserRole.HUB_MANAGER) {
            UUID managerHubId = userServiceClient.getUser(UUID.fromString(userId)).getData().getHubId();
            if (resourceHubId != null && !managerHubId.equals(resourceHubId)) {
                throw new DeliveryManagerException(DeliveryManagerErrorCode.MANAGER_FORBIDDEN);
            }
            return;
        }
        throw new DeliveryManagerException(DeliveryManagerErrorCode.MANAGER_FORBIDDEN);
    }

    // 상세 조회: MASTER, HUB_MANAGER(담당 허브), DELIVERY_MANAGER(본인)만 허용
    private void checkReadPermission(UserRole role, String userId, DeliveryManager manager) {
        if (role == UserRole.MASTER) return;
        if (role == UserRole.HUB_MANAGER) {
            UUID managerHubId = userServiceClient.getUser(UUID.fromString(userId)).getData().getHubId();
            if (!managerHubId.equals(manager.getHubId())) {
                throw new DeliveryManagerException(DeliveryManagerErrorCode.MANAGER_FORBIDDEN);
            }
            return;
        }
        if (role == UserRole.DELIVERY_MANAGER && manager.getUserId().toString().equals(userId)) return;
        throw new DeliveryManagerException(DeliveryManagerErrorCode.MANAGER_FORBIDDEN);
    }

    private DeliveryManager findActiveManager(UUID id) {
        return deliveryManagerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DeliveryManagerException(DeliveryManagerErrorCode.MANAGER_NOT_FOUND));
    }

    private void validateHubExists(UUID hubId) {
        try {
            hubServiceClient.getHub(hubId);
        } catch (FeignException.NotFound e) {
            throw new DeliveryManagerException(DeliveryManagerErrorCode.HUB_NOT_FOUND);
        }
    }
}
