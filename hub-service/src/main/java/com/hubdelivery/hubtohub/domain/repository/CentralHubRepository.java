package com.hubdelivery.hubtohub.domain.repository;

import com.hubdelivery.hubtohub.domain.entity.CentralHubEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 중앙허브 Repository
 */
@Repository
public interface CentralHubRepository extends JpaRepository<CentralHubEntity, UUID> {


    /**
     * 모든 중앙허브 조회
     */
    List<CentralHubEntity> findAll();

    @Query("SELECT h " +
            "FROM CentralHubEntity h " +
            "WHERE h.hub.id = :id AND h.deletedAt IS NULL")
    Optional<CentralHubEntity> findByHubIdIsActive(UUID id);
}
