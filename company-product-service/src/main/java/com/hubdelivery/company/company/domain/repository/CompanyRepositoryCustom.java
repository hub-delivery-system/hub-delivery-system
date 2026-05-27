package com.hubdelivery.company.company.domain.repository;

import com.hubdelivery.company.company.domain.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyRepositoryCustom {

    Page<Company> searchCompanies(String keyword, Pageable pageable);
}
