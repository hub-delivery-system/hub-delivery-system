package com.hubdelivery.deliveryservice.deliverymanager.domain.repository;

import com.hubdelivery.deliveryservice.deliverymanager.domain.entity.DeliveryManager;
import com.hubdelivery.deliveryservice.deliverymanager.domain.type.DeliveryManagerType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryManagerRepository extends JpaRepository<DeliveryManager, UUID> {

    // 신규 담당자 순번 = 허브+타입 기준 현재 최대 순번 + 1. 담당자 없으면 0 반환 → 첫 순번은 1
    @Query("SELECT COALESCE(MAX(dm.sequence), 0) FROM DeliveryManager dm " +
           "WHERE dm.hubId = :hubId AND dm.type = :type AND dm.deletedAt IS NULL")
    int findMaxSequence(@Param("hubId") UUID hubId, @Param("type") DeliveryManagerType type);

    // 순환 배정 1단계: 현재 순번 이후의 가장 작은 순번 담당자 조회
    @Query("SELECT dm FROM DeliveryManager dm " +
           "WHERE dm.hubId = :hubId AND dm.type = :type " +
           "AND dm.sequence > :currentSequence AND dm.deletedAt IS NULL " +
           "ORDER BY dm.sequence ASC")
    Page<DeliveryManager> findNextAfter(
            @Param("hubId") UUID hubId,
            @Param("type") DeliveryManagerType type,
            @Param("currentSequence") int currentSequence,
            Pageable pageable);

    // 순환 배정 2단계: 다음 담당자가 없을 때 처음으로 wrap-around
    @Query("SELECT dm FROM DeliveryManager dm " +
           "WHERE dm.hubId = :hubId AND dm.type = :type AND dm.deletedAt IS NULL " +
           "ORDER BY dm.sequence ASC")
    Page<DeliveryManager> findFirstByHubIdAndType(
            @Param("hubId") UUID hubId,
            @Param("type") DeliveryManagerType type,
            Pageable pageable);

    Page<DeliveryManager> findAllByDeletedAtIsNull(Pageable pageable);

    // HUB_MANAGER 목록 조회: 담당 허브 소속 담당자만 반환
    Page<DeliveryManager> findAllByHubIdAndDeletedAtIsNull(UUID hubId, Pageable pageable);

    Optional<DeliveryManager> findByIdAndDeletedAtIsNull(UUID id);

    // HUB_DELIVERY 전용 순환 배정 1단계: hubId 조건 없이 type + sequence > current 조회
    @Query("SELECT dm FROM DeliveryManager dm WHERE dm.type = :type " +
           "AND dm.sequence > :currentSequence AND dm.deletedAt IS NULL ORDER BY dm.sequence ASC")
    Page<DeliveryManager> findNextByTypeAfter(
            @Param("type") DeliveryManagerType type,
            @Param("currentSequence") int currentSequence,
            Pageable pageable);

    // HUB_DELIVERY 전용 순환 배정 2단계: wrap-around (type 기준)
    @Query("SELECT dm FROM DeliveryManager dm WHERE dm.type = :type " +
           "AND dm.deletedAt IS NULL ORDER BY dm.sequence ASC")
    Page<DeliveryManager> findFirstByType(
            @Param("type") DeliveryManagerType type,
            Pageable pageable);
}
