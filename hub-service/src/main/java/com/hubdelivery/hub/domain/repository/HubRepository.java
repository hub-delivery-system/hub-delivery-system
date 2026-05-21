package com.hubdelivery.hub.domain.repository;

import com.hubdelivery.hub.domain.entity.HubEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface HubRepository extends JpaRepository<HubEntity, UUID> {


    //Hubs 조회
    @Query("SELECT h FROM HubEntity h WHERE h.deletedAt IS NULL")
    Page<HubEntity> findAllActive(Pageable pageable);

    // Id로 조회
    @Query("SELECT h FROM HubEntity h WHERE h.id = :id AND h.deletedAt IS NULL")
    Optional<HubEntity> findByIdActive(@Param("id") UUID id);


    boolean existsByLatitudeAndLongitude(BigDecimal latitude,BigDecimal longitude);
}
