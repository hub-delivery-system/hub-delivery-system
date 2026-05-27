package com.hubdelivery.company.company.application.service;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.company.domain.entity.Company;
import com.hubdelivery.company.company.domain.exception.CompanyNotFoundException;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.company.presentation.dto.request.CompanyCreateRequestDto;
import com.hubdelivery.company.company.presentation.dto.request.CompanyUpdateRequestDto;
import com.hubdelivery.company.company.presentation.dto.response.CompanyResponseDto;
import com.hubdelivery.company.global.util.SearchPageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.hubdelivery.company.global.util.SearchSortPolicy.COMPANY;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    /** 업체 생성 로직 */
    @Transactional
    public CompanyResponseDto createCompany(UUID userId, UserRole userRole, CompanyCreateRequestDto request) {
        // TODO: userId/userRole 기반 scope 권한 검증
        // TODO: hub-service 연동 후 hubId 존재 여부 검증
        // TODO: 여러 검증 로직 추가
        Company company = companyRepository.save(request.toEntity());
        return CompanyResponseDto.from(company);
    }

    /** 업체 전체 조회 로직 */
    public PageResponse<CompanyResponseDto> getAllCompanies(String keyword, Integer page, Integer size, String sort) {
        String normalizedKeyword = SearchPageableUtils.normalizeKeyword(keyword);
        Pageable pageable = SearchPageableUtils.createPageable(page, size, sort, COMPANY);

        return PageResponse.from(companyRepository.searchCompanies(normalizedKeyword, pageable)
                .map(CompanyResponseDto::from));
    }

    /** 업체 상세 조회 로직 */
    public CompanyResponseDto getCompany(UUID companyId) {
        return CompanyResponseDto.from(companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(CompanyNotFoundException::new));
    }

    /** 업체 수정 로직 */
    @Transactional
    public CompanyResponseDto updateCompany(UUID userId, UserRole userRole, UUID companyId, CompanyUpdateRequestDto request) {
        // TODO: userId/userRole 기반 scope 권한 검증
        // TODO: hub-service 연동 후 hubId 존재 여부 검증
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(CompanyNotFoundException::new);

        company.update(request.companyName(), request.companyType(), request.hubId(), request.address());

        return CompanyResponseDto.from(company);
    }

    /** 업체 삭제 로직 */
    @Transactional
    public void deleteCompany(UUID userId, UserRole userRole, UUID companyId) {
        // TODO: userId/userRole 기반 scope 권한 검증
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(CompanyNotFoundException::new);

        // TODO: user-service 연동 후 userId 대신 username 으로 기록
        company.softDelete(userId.toString());
    }
}
