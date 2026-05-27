package com.hubdelivery.hubtohub.domain.repository;

import com.hubdelivery.hubtohub.domain.entity.HubToHubEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubToHubRepository extends JpaRepository<HubToHubEntity, UUID> {

    @Query("""
           SELECT r FROM HubToHubEntity r
           WHERE r.startHubId = :startHubId
           AND r.endHubId = :endHubId
           AND r.deletedAt IS NULL
           """)
    Optional<HubToHubEntity> findActiveRoute(
            @Param("startHubId") UUID startHubId,
            @Param("endHubId") UUID endHubId
    );

    @Query("""
           SELECT r FROM HubToHubEntity r
           WHERE (r.startHubId = :hubId OR r.endHubId = :hubId)
           AND r.deletedAt IS NULL
           """)
    List<HubToHubEntity> findAllByHubId(@Param("hubId") UUID hubId);
}
