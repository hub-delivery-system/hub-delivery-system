package com.hubdelivery.company.company.application.service;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.company.domain.entity.Company;
import com.hubdelivery.company.company.domain.exception.CompanyAccessDeniedException;
import com.hubdelivery.company.company.domain.exception.CompanyHubIntegrationException;
import com.hubdelivery.company.company.domain.exception.CompanyHubNotFoundException;
import com.hubdelivery.company.company.domain.exception.CompanyNotFoundException;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.company.presentation.dto.request.CompanyCreateRequestDto;
import com.hubdelivery.company.company.presentation.dto.request.CompanyUpdateRequestDto;
import com.hubdelivery.company.company.presentation.dto.response.CompanyResponseDto;
import com.hubdelivery.company.global.infrastructure.client.hub.HubClient;
import com.hubdelivery.company.global.infrastructure.client.user.dto.UserResponse;
import com.hubdelivery.company.global.util.SearchPageableUtils;
import feign.FeignException;
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
    private final HubClient hubClient;
    // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
    // private final UserAuthorizationValidator userAuthorizationValidator;

    /** 업체 생성 로직 */
    @Transactional
    public CompanyResponseDto createCompany(UUID userId, UserRole userRole, CompanyCreateRequestDto request) {
        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // UserResponse user = userAuthorizationValidator.validateCurrentUser(userId, userRole);
        // validateCompanyCreateAuthority(user, request.hubId());
        validateHubExists(userId, userRole, request.hubId());

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
        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // UserResponse user = userAuthorizationValidator.validateCurrentUser(userId, userRole);
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(CompanyNotFoundException::new);

        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // validateCompanyUpdateAuthority(user, company, companyId, request.hubId());
        validateHubExists(userId, userRole, request.hubId());

        company.update(request.companyName(), request.companyType(), request.hubId(), request.address());

        return CompanyResponseDto.from(company);
    }

    /** 업체 삭제 로직 */
    @Transactional
    public void deleteCompany(UUID userId, UserRole userRole, UUID companyId) {
        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // UserResponse user = userAuthorizationValidator.validateCurrentUser(userId, userRole);
        Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(CompanyNotFoundException::new);

        // TODO: user-service 의 GET /api/v1/users/{user_id} 구현 완료 후 재활성화
        // validateCompanyDeleteAuthority(user, company);

        // TODO: user-service 연동 후 userId 대신 username 으로 기록
        company.softDelete(userId.toString());
    }

    private void validateCompanyCreateAuthority(UserResponse user, UUID requestHubId) {
        if (user.role() == UserRole.MASTER) {
            return;
        }

        if (user.role() == UserRole.HUB_MANAGER && requestHubId.equals(user.hubId())) {
            return;
        }

        throw new CompanyAccessDeniedException();
    }

    private void validateCompanyUpdateAuthority(UserResponse user, Company company, UUID companyId, UUID requestHubId) {
        if (user.role() == UserRole.MASTER) {
            return;
        }

        if (user.role() == UserRole.HUB_MANAGER
                && company.getHubId().equals(user.hubId())
                && requestHubId.equals(user.hubId())) {
            return;
        }

        if (user.role() == UserRole.COMPANY_MANAGER && companyId.equals(user.companyId())) {
            return;
        }

        throw new CompanyAccessDeniedException();
    }

    private void validateCompanyDeleteAuthority(UserResponse user, Company company) {
        if (user.role() == UserRole.MASTER) {
            return;
        }

        if (user.role() == UserRole.HUB_MANAGER && company.getHubId().equals(user.hubId())) {
            return;
        }

        throw new CompanyAccessDeniedException();
    }

    private void validateHubExists(UUID userId, UserRole userRole, UUID hubId) {
        try {
            hubClient.getHub(userId, userRole, hubId);
        } catch (FeignException.NotFound e) {
            throw new CompanyHubNotFoundException();
        } catch (FeignException e) {
            throw new CompanyHubIntegrationException();
        }
    }
}
