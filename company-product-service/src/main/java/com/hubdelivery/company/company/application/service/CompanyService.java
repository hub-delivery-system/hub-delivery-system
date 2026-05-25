package com.hubdelivery.company.company.application.service;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.company.company.domain.entity.Company;
import com.hubdelivery.company.company.domain.exception.CompanyNotFoundException;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.company.presentation.dto.request.CompanyCreateRequestDto;
import com.hubdelivery.company.company.presentation.dto.request.CompanyUpdateRequestDto;
import com.hubdelivery.company.company.presentation.dto.response.CompanyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private static final String DEFAULT_SORT_PROPERTY = "createdAt";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of("createdAt", "updatedAt", "companyName");

    private final CompanyRepository companyRepository;

    /** 업체 생성 로직 */
    @Transactional
    public CompanyResponseDto createCompany(CompanyCreateRequestDto request) {
        // TODO: hub-service 연동 확정 후 hubId 존재 여부와 HUB_MANAGER 담당 허브 여부를 검증한다.
        // TODO: 여러 검증 로직 추가
        Company company = companyRepository.save(request.toEntity());
        return CompanyResponseDto.from(company);
    }

    /** 업체 전체 조회 로직 */
    public PageResponse<CompanyResponseDto> getAllCompanies(String keyword, Integer page, Integer size, String sort) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Pageable pageable = createPageable(page, size, sort);

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
    public CompanyResponseDto updateCompany(UUID companyId, CompanyUpdateRequestDto request) {
        // TODO: hub-service 연동 확정 후 hubId 존재 여부와 HUB_MANAGER 담당 허브 여부를 검증한다.
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(CompanyNotFoundException::new);

        company.update(request.companyName(), request.companyType(), request.hubId(), request.address());

        return CompanyResponseDto.from(company);
    }

    /** 업체 삭제 로직 */
    @Transactional
    public void deleteCompany(UUID companyId, String deletedBy) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(CompanyNotFoundException::new);

        company.softDelete(deletedBy);
    }

    private Pageable createPageable(Integer page, Integer size, String sort) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? PageableUtils.DEFAULT_SIZE : size;
        Pageable pageable = PageableUtils.createPageable(pageNumber, pageSize);

        if (!PageableUtils.hasKeyword(sort)) {
            return pageable;
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), resolveSort(sort));
    }

    private Sort resolveSort(String sort) {
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            property = DEFAULT_SORT_PROPERTY;
        }

        Sort.Direction direction = parts.length < 2
                ? DEFAULT_SORT_DIRECTION
                : Sort.Direction.fromOptionalString(parts[1].trim()).orElse(DEFAULT_SORT_DIRECTION);

        return Sort.by(direction, property);
    }

    private String normalizeKeyword(String keyword) {
        if (!PageableUtils.hasKeyword(keyword)) {
            return null;
        }

        return keyword.trim();
    }
}
