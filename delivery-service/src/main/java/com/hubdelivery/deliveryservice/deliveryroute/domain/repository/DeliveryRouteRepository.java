package com.hubdelivery.deliveryservice.deliveryroute.domain.repository;

import com.hubdelivery.deliveryservice.deliveryroute.domain.entity.DeliveryRoute;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRouteRepository extends JpaRepository<DeliveryRoute, UUID> {

    Optional<DeliveryRoute> findByIdAndDeletedAtIsNull(UUID id);

    // 배송 상세 조회 시 경로 전체 로드 (sequence 정렬)
    List<DeliveryRoute> findAllByDeliveryIdAndDeletedAtIsNullOrderBySequenceAsc(UUID deliveryId);

    // MASTER / COMPANY_MANAGER: 배송의 모든 경로 페이징
    Page<DeliveryRoute> findAllByDeliveryIdAndDeletedAtIsNull(UUID deliveryId, Pageable pageable);

    // HUB_MANAGER: 담당 허브가 출발지 또는 도착지인 경로
    @Query("SELECT r FROM DeliveryRoute r WHERE r.deliveryId = :deliveryId " +
           "AND (r.startHubId = :hubId OR r.endHubId = :hubId) AND r.deletedAt IS NULL")
    Page<DeliveryRoute> findAllByDeliveryIdAndHubIdAndDeletedAtIsNull(
            @Param("deliveryId") UUID deliveryId,
            @Param("hubId") UUID hubId,
            Pageable pageable);

    // DELIVERY_MANAGER: 자신이 담당하는 경로
    Page<DeliveryRoute> findAllByDeliveryIdAndDeliveryManagerIdAndDeletedAtIsNull(
            UUID deliveryId, UUID deliveryManagerId, Pageable pageable);

    // HUB_DELIVERY_MANAGER 크로스 요청 순환 배정용: 가장 최근 배정된 HUB_DM ID 조회
    @Query("SELECT r.deliveryManagerId FROM DeliveryRoute r " +
           "WHERE r.deliveryManagerId IS NOT NULL AND r.deletedAt IS NULL " +
           "ORDER BY r.createdAt DESC")
    Page<UUID> findLatestHubDeliveryManagerId(Pageable pageable);
}
