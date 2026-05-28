package com.hubdelivery.hubtohub.domain.repository;

import com.hubdelivery.hubtohub.domain.entity.HubTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubTransferRepository extends JpaRepository<HubTransferEntity, UUID> {

    @Query("""
           SELECT r FROM HubTransferEntity r
           WHERE r.startHubId = :startHubId
           AND r.endHubId = :endHubId
           AND r.deletedAt IS NULL
           """)
    Optional<HubTransferEntity> findActiveRoute(
            @Param("startHubId") UUID startHubId,
            @Param("endHubId") UUID endHubId
    );

    @Query("""
           SELECT r FROM HubTransferEntity r
           WHERE (r.startHubId = :hubId OR r.endHubId = :hubId)
           AND r.deletedAt IS NULL
           """)
    List<HubTransferEntity> findAllByHubId(@Param("hubId") UUID hubId);

    Optional<HubTransferEntity> findByStartHubIdAndEndHubId(UUID startHubId, UUID endHubId);

    boolean existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(UUID fromHubId, UUID toHubId);
}
