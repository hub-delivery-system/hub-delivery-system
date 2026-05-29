package com.hubdelivery.hubtohub.domain.repository;

import com.hubdelivery.hubtohub.domain.entity.HubTransferEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubTransferRepository extends JpaRepository<HubTransferEntity, UUID> {


    @Query("""
           SELECT r FROM HubTransferEntity r
           WHERE r.id = :id
           AND r.deletedAt IS NULL
           """)
    Optional<HubTransferEntity> findActiveRouteById(UUID id);

    boolean existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(UUID fromHubId, UUID toHubId);


    @Query("SELECT DISTINCT h FROM HubTransferEntity h " +
            "LEFT JOIN FETCH h.waypoints w " +
            "WHERE h.deletedAt IS NULL " +
            "AND (:fromHubId IS NULL OR h.startHubId = :fromHubId) " +
            "AND (:toHubId IS NULL OR h.endHubId = :toHubId) " +
            "ORDER BY h.createdAt DESC")
    Page<HubTransferEntity> findByFilters(
            @Param("fromHubId") UUID fromHubId,
            @Param("toHubId") UUID toHubId,
            Pageable pageable
    );

    /**
     * 페이지네이션용 ID 조회
     */
    @Query("SELECT h.id FROM HubTransferEntity h " +
            "WHERE h.deletedAt IS NULL " +
            "AND (:fromHubId IS NULL OR h.startHubId = :fromHubId) " +
            "AND (:toHubId IS NULL OR h.endHubId = :toHubId)")
    Page<UUID> findIdsByFilters(
            @Param("fromHubId") UUID fromHubId,
            @Param("toHubId") UUID toHubId,
            Pageable pageable
    );


    @Query("SELECT h FROM HubTransferEntity h " +
            "LEFT JOIN FETCH h.waypoints w " +
            "LEFT JOIN FETCH w.centralHub cb " +
            "LEFT JOIN FETCH cb.hub " +
            "WHERE h.id IN :ids " +
            "AND h.deletedAt IS NULL " +
            "ORDER BY h.createdAt DESC")
    List<HubTransferEntity> findByIdsFetchWaypointsWithAll(@Param("ids") List<UUID> ids);

    @Query("SELECT h FROM HubTransferEntity h " +
            "LEFT JOIN FETCH h.waypoints w " +
            "LEFT JOIN FETCH w.centralHub cb " +
            "LEFT JOIN FETCH cb.hub " +
            "WHERE h.id = :id " +
            "AND h.deletedAt IS NULL")
    Optional<HubTransferEntity> findByIdFetchWaypoints(@Param("id") UUID id);


}
