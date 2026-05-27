package com.hubdelivery.company.company.domain.repository;

import com.hubdelivery.company.company.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID>, CompanyRepositoryCustom {

    Optional<Company> findByIdAndDeletedAtIsNull(UUID id);
}
