package com.hubdelivery.slack.infrastructure.persistence;

import com.hubdelivery.slack.domain.entity.CompanyDeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaCompanyDeliveryRouteRepository
        extends JpaRepository<CompanyDeliveryRoute, UUID> {

    List<CompanyDeliveryRoute> findByDeliveryManagerId(UUID deliveryManagerId);
}
