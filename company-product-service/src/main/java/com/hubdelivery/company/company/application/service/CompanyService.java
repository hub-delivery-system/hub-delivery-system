package com.hubdelivery.company.company.application.service;

import com.hubdelivery.company.company.domain.entity.Company;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.company.presentation.dto.request.CompanyCreateRequestDto;
import com.hubdelivery.company.company.presentation.dto.response.CompanyCreateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyCreateResponseDto createCompany(CompanyCreateRequestDto request) {
        // TODO: hub-service 연동 확정 후 hubId 존재 여부와 HUB_MANAGER 담당 허브 여부를 검증한다.
        // TODO: 여러 검증 로직 추가
        Company company = companyRepository.save(request.toEntity());
        return CompanyCreateResponseDto.from(company);
    }
}
