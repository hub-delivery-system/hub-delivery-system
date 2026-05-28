package com.hubdelivery.hubtohub.domain.repository;

import com.hubdelivery.hubtohub.domain.entity.HubTransferWaypointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HubToHubWaypointRepository extends JpaRepository<HubTransferWaypointEntity, UUID> {
    List<HubTransferWaypointEntity> findByHubToHubIdOrderBySequence(UUID id);
}
