package com.hubdelivery.hub.domain.repository;

import com.hubdelivery.hub.domain.entity.HubEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 키워드 조회
    @Query("SELECT h FROM HubEntity h " +
            "WHERE h.deletedAt IS NULL " +
            "AND (:keyword IS NULL OR h.hubName LIKE %:keyword%)")
    Page<HubEntity> searchHubs(
            @Param("keyword") String keyword,
            Pageable pageable
    );


    boolean existsByLatitudeAndLongitude(BigDecimal latitude,BigDecimal longitude);

    @Query("SELECT COUNT(h) > 0 FROM HubEntity h " +
            "WHERE h.latitude = :latitude AND h.longitude = :longitude " +
            "AND h.id != :id AND h.deletedAt IS NULL")
    boolean existsByLatitudeAndLongitudeExcludingId(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("id") UUID id
    );}
