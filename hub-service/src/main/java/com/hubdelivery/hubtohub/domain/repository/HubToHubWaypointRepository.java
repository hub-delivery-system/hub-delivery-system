package com.hubdelivery.hubtohub.domain.repository;

import com.hubdelivery.hubtohub.domain.entity.HubTransferWaypointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface HubToHubWaypointRepository extends JpaRepository<HubTransferWaypointEntity, UUID> {

    @Query("SELECT w FROM HubTransferWaypointEntity w " +
            "WHERE w.hubTransfer.id = :id " +
            "ORDER BY w.sequence ASC")
    List<HubTransferWaypointEntity> findByHubToHubIdOrderBySequence(UUID id);

    @Modifying
    @Query("DELETE FROM HubTransferWaypointEntity w " +
            "WHERE w.hubTransfer.id = :transferId")
    void deleteByHubToHubId(UUID transferId);


    @Query("SELECT w FROM HubTransferWaypointEntity w " +
            "WHERE w.hubTransfer.id = :hubToHubId " +
            "AND w.deletedAt IS NULL " +
            "ORDER BY w.sequence ASC")
    List<HubTransferWaypointEntity> findByHubToHubIdAndDeletedAtIsNullOrderBySequence(UUID hubToHubId);
}
