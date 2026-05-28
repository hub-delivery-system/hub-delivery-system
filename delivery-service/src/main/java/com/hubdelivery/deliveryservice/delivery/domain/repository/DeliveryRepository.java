package com.hubdelivery.deliveryservice.delivery.domain.repository;

import com.hubdelivery.deliveryservice.delivery.domain.entity.Delivery;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID>, DeliveryRepositoryCustom {

    Optional<Delivery> findByIdAndDeletedAtIsNull(UUID id);

    Page<Delivery> findAllByDeletedAtIsNull(Pageable pageable);

    // HUB_MANAGER: 담당 허브가 출발지 또는 도착지인 배송 목록
    @Query("SELECT d FROM Delivery d WHERE (d.startHubId = :hubId OR d.endHubId = :hubId) AND d.deletedAt IS NULL")
    Page<Delivery> findAllByHubIdAndDeletedAtIsNull(@Param("hubId") UUID hubId, Pageable pageable);

    // DELIVERY_MANAGER: 자신이 담당하는 배송 목록
    Page<Delivery> findAllByDeliveryManagerIdAndDeletedAtIsNull(UUID deliveryManagerId, Pageable pageable);

    // COMPANY_DELIVERY_MANAGER 순환 배정용: endHubId 기준 가장 최근 배정된 담당자 ID 조회
    @Query("SELECT d.deliveryManagerId FROM Delivery d " +
           "WHERE d.endHubId = :endHubId AND d.deliveryManagerId IS NOT NULL AND d.deletedAt IS NULL " +
           "ORDER BY d.createdAt DESC")
    Page<UUID> findLatestDeliveryManagerIdByEndHub(@Param("endHubId") UUID endHubId, Pageable pageable);
}
