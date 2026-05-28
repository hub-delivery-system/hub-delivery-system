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
import com.hubdelivery.deliveryservice.deliverymanager.presentation.dto.DeliveryManagerSearchCondition;
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

    private static final int MAX_CAPACITY = 10;

    @Transactional
    public DeliveryManagerResponse create(DeliveryManagerCreateRequest request, String userId, UserRole role) {
        checkWritePermission(role, userId, request.getHubId());
        validateRequest(request);
        checkCapacity(request.getType(), request.getHubId());

        int sequence = calculateSequence(request.getType(), request.getHubId());

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
    public PageResponse<DeliveryManagerResponse> getAll(
            int page, int size,
            DeliveryManagerSearchCondition cond,
            String userId, UserRole role) {
        checkWritePermission(role, userId, null);
        Pageable pageable = PageableUtils.createPageable(page, size);

        // HUB_MANAGER는 담당 허브 소속 담당자만 조회 (fixedHubId 강제 적용)
        UUID fixedHubId = null;
        if (role == UserRole.HUB_MANAGER) {
            fixedHubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
        }

        return PageResponse.from(
                deliveryManagerRepository.searchManagers(cond, fixedHubId, pageable)
                        .map(DeliveryManagerResponse::from)
        );
    }

    @Transactional
    public DeliveryManagerResponse update(UUID id, DeliveryManagerUpdateRequest request, String userId, UserRole role) {
        DeliveryManager manager = findActiveManager(id);
        checkWritePermission(role, userId, manager.getHubId());

        if (request.getHubId() != null) {
            validateHubExists(request.getHubId());
        }

        // hubId 또는 type이 변경된 경우 새 기준으로 순번 재배정
        boolean changed = !java.util.Objects.equals(manager.getHubId(), request.getHubId())
                || !manager.getType().equals(request.getType());
        int sequence = changed
                ? calculateSequence(request.getType(), request.getHubId())
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
     * 순환 배정 (COMPANY_DELIVERY 전용): hubId 소속 담당자 중 현재 순번 이후 → 없으면 wrap-around
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

    /**
     * 순환 배정 (HUB_DELIVERY 전용): hubId 없이 type 기준으로 순환 배정
     */
    @Transactional(readOnly = true)
    public DeliveryManagerResponse assignNextByType(DeliveryManagerType type, int currentSequence) {
        Pageable first = PageRequest.of(0, 1);

        return deliveryManagerRepository.findNextByTypeAfter(type, currentSequence, first)
                .getContent()
                .stream()
                .findFirst()
                .or(() -> deliveryManagerRepository.findFirstByType(type, first)
                        .getContent()
                        .stream()
                        .findFirst())
                .map(DeliveryManagerResponse::from)
                .orElseThrow(() -> new DeliveryManagerException(DeliveryManagerErrorCode.NO_AVAILABLE_MANAGER));
    }

    /**
     * 담당자 ID로 sequence 조회 (순환 배정 currentSequence 추적용)
     */
    @Transactional(readOnly = true)
    public int getSequenceById(UUID managerId) {
        return deliveryManagerRepository.findByIdAndDeletedAtIsNull(managerId)
                .map(DeliveryManager::getSequence)
                .orElse(0);
    }

    // COMPANY_DELIVERY는 hubId 필수 + 허브 존재 검증, HUB_DELIVERY는 hubId 불필요
    private void validateRequest(DeliveryManagerCreateRequest request) {
        if (request.getType() == DeliveryManagerType.COMPANY_DELIVERY) {
            if (request.getHubId() == null) {
                throw new DeliveryManagerException(DeliveryManagerErrorCode.HUB_NOT_FOUND);
            }
            validateHubExists(request.getHubId());
        }
    }

    // 타입별 정원 체크 (FOR UPDATE로 행 직접 잠금 → 동시 승인 시 초과 방지)
    private void checkCapacity(DeliveryManagerType type, UUID hubId) {
        int count = (type == DeliveryManagerType.HUB_DELIVERY)
                ? deliveryManagerRepository.findAllByTypeForUpdate(type).size()
                : deliveryManagerRepository.findAllByHubIdAndTypeForUpdate(hubId, type).size();

        if (count >= MAX_CAPACITY) {
            throw new DeliveryManagerException(DeliveryManagerErrorCode.CAPACITY_EXCEEDED);
        }
    }

    // 타입별 순번 계산 (HUB_DELIVERY는 전체 기준, COMPANY_DELIVERY는 허브 기준)
    private int calculateSequence(DeliveryManagerType type, UUID hubId) {
        return (type == DeliveryManagerType.HUB_DELIVERY)
                ? deliveryManagerRepository.findMaxSequenceByType(type) + 1
                : deliveryManagerRepository.findMaxSequence(hubId, type) + 1;
    }

    // 생성·수정·삭제·목록 조회: MASTER 또는 HUB_MANAGER(담당 허브)만 허용
    private void checkWritePermission(UserRole role, String userId, UUID resourceHubId) {
        if (role == UserRole.MASTER) return;
        if (role == UserRole.HUB_MANAGER) {
            UUID managerHubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
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
            UUID managerHubId = userServiceClient.getUser(UUID.fromString(userId)).data().getHubId();
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
