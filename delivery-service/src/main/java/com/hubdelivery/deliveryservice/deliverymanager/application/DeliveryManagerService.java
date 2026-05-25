package com.hubdelivery.deliveryservice.deliverymanager.application;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.deliveryservice.deliverymanager.domain.entity.DeliveryManager;
import com.hubdelivery.deliveryservice.deliverymanager.domain.exception.DeliveryManagerErrorCode;
import com.hubdelivery.deliveryservice.deliverymanager.domain.exception.DeliveryManagerException;
import com.hubdelivery.deliveryservice.deliverymanager.domain.repository.DeliveryManagerRepository;
import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.HubServiceClient;
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

    @Transactional
    public DeliveryManagerResponse create(DeliveryManagerCreateRequest request) {
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
    public DeliveryManagerResponse getById(UUID id) {
        return DeliveryManagerResponse.from(findActiveManager(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<DeliveryManagerResponse> getAll(int page, int size) {
        Pageable pageable = PageableUtils.createPageable(page, size);
        return PageResponse.from(
                deliveryManagerRepository.findAllByDeletedAtIsNull(pageable)
                        .map(DeliveryManagerResponse::from)
        );
    }

    @Transactional
    public DeliveryManagerResponse update(UUID id, DeliveryManagerUpdateRequest request) {
        DeliveryManager manager = findActiveManager(id);

        // hubId가 변경된 경우에만 허브 검증 및 순번 재배정
        boolean hubChanged = !manager.getHubId().equals(request.getHubId());
        if (hubChanged) {
            validateHubExists(request.getHubId());
        }

        int sequence = hubChanged
                ? deliveryManagerRepository.findMaxSequence(request.getHubId(), request.getType()) + 1
                : manager.getSequence();

        manager.update(request.getHubId(), request.getCompanyId(), request.getType(), sequence);
        return DeliveryManagerResponse.from(manager);
    }

    @Transactional
    public void delete(UUID id, String deletedBy) {
        DeliveryManager manager = findActiveManager(id);
        manager.softDelete(deletedBy);
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

    // 소프트 삭제되지 않은 담당자 조회
    private DeliveryManager findActiveManager(UUID id) {
        return deliveryManagerRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new DeliveryManagerException(DeliveryManagerErrorCode.MANAGER_NOT_FOUND));
    }

    // Feign 호출로 허브 존재 여부 검증
    private void validateHubExists(UUID hubId) {
        try {
            hubServiceClient.getHub(hubId);
        } catch (FeignException.NotFound e) {
            throw new DeliveryManagerException(DeliveryManagerErrorCode.HUB_NOT_FOUND);
        }
    }
}
