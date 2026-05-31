package com.hubdelivery.company.company.application.service;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.company.domain.entity.Company;
import com.hubdelivery.company.company.domain.exception.CompanyAccessDeniedException;
import com.hubdelivery.company.company.domain.exception.CompanyHubIntegrationException;
import com.hubdelivery.company.company.domain.exception.CompanyHubNotFoundException;
import com.hubdelivery.company.company.domain.exception.CompanyNotFoundException;
import com.hubdelivery.company.company.domain.repository.CompanyRepository;
import com.hubdelivery.company.company.domain.type.CompanyType;
import com.hubdelivery.company.company.presentation.dto.request.CompanyCreateRequestDto;
import com.hubdelivery.company.company.presentation.dto.request.CompanyUpdateRequestDto;
import com.hubdelivery.company.company.presentation.dto.response.CompanyResponseDto;
import com.hubdelivery.company.global.infrastructure.client.hub.HubClient;
import com.hubdelivery.company.global.infrastructure.client.hub.dto.HubResponse;
import com.hubdelivery.company.global.infrastructure.client.user.dto.UserResponse;
import com.hubdelivery.company.global.security.UserAuthorizationValidator;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @InjectMocks
    private CompanyService companyService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private HubClient hubClient;

    @Mock
    private UserAuthorizationValidator userAuthorizationValidator;

    private final UUID userId = UUID.randomUUID();
    private final UserRole userRole = UserRole.MASTER;

    @Nested
    @DisplayName("업체 생성")
    class CreateCompany {

        @Test
        @DisplayName("업체 생성 성공")
        void createCompany_success() {
            // given
            UUID hubId = UUID.randomUUID();
            CompanyCreateRequestDto request = createRequest("모니터 업체", CompanyType.PRODUCER, hubId, "서울시 중구");

            givenCurrentUser(UserRole.MASTER, null, null);
            givenHubExists(hubId);
            given(companyRepository.save(any(Company.class)))
                    .willAnswer(invocation -> {
                        Company company = invocation.getArgument(0);
                        setId(company, UUID.randomUUID());
                        return company;
                    });

            // when
            CompanyResponseDto response = companyService.createCompany(userId, userRole, request);

            // then
            assertThat(response.companyName()).isEqualTo("모니터 업체");
            assertThat(response.companyType()).isEqualTo(CompanyType.PRODUCER);
            assertThat(response.hubId()).isEqualTo(hubId);
            assertThat(response.address()).isEqualTo("서울시 중구");

            ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
            verify(companyRepository).save(captor.capture());
            assertThat(captor.getValue().getCompanyName()).isEqualTo("모니터 업체");
        }

        @Test
        @DisplayName("업체 생성 실패 - 존재하지 않는 허브")
        void createCompany_hubNotFound() {
            // given
            UUID hubId = UUID.randomUUID();
            CompanyCreateRequestDto request = createRequest("모니터 업체", CompanyType.PRODUCER, hubId, "서울시 중구");

            givenCurrentUser(UserRole.MASTER, null, null);
            given(hubClient.getHub(userId, userRole, hubId)).willThrow(feignException(404));

            // when & then
            assertThatThrownBy(() -> companyService.createCompany(userId, userRole, request))
                    .isInstanceOf(CompanyHubNotFoundException.class);

            verify(companyRepository, never()).save(any());
        }

        @Test
        @DisplayName("업체 생성 실패 - 허브 서버 연동 실패")
        void createCompany_hubIntegrationFailed() {
            // given
            UUID hubId = UUID.randomUUID();
            CompanyCreateRequestDto request = createRequest("모니터 업체", CompanyType.PRODUCER, hubId, "서울시 중구");

            givenCurrentUser(UserRole.MASTER, null, null);
            given(hubClient.getHub(userId, userRole, hubId)).willThrow(feignException(500));

            // when & then
            assertThatThrownBy(() -> companyService.createCompany(userId, userRole, request))
                    .isInstanceOf(CompanyHubIntegrationException.class);

            verify(companyRepository, never()).save(any());
        }

        @Test
        @DisplayName("업체 생성 실패 - HUB_MANAGER 담당 허브 불일치")
        void createCompany_hubManagerDifferentHubDenied() {
            // given
            UUID requestHubId = UUID.randomUUID();
            UUID userHubId = UUID.randomUUID();
            CompanyCreateRequestDto request = createRequest("모니터 업체", CompanyType.PRODUCER, requestHubId, "서울시 중구");

            givenCurrentUser(UserRole.HUB_MANAGER, userHubId, null);

            // when & then
            assertThatThrownBy(() -> companyService.createCompany(userId, UserRole.HUB_MANAGER, request))
                    .isInstanceOf(CompanyAccessDeniedException.class);

            verifyNoInteractions(hubClient);
            verify(companyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("업체 단건 조회")
    class GetCompany {

        @Test
        @DisplayName("업체 단건 조회 성공")
        void getCompany_success() {
            // given
            UUID companyId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            Company company = company(companyId, "모니터 업체", CompanyType.PRODUCER, hubId, "서울시 중구");

            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));

            // when
            CompanyResponseDto response = companyService.getCompany(companyId);

            // then
            assertThat(response.id()).isEqualTo(companyId);
            assertThat(response.companyName()).isEqualTo("모니터 업체");
            assertThat(response.hubId()).isEqualTo(hubId);
        }

        @Test
        @DisplayName("업체 단건 조회 실패 - 존재하지 않는 업체")
        void getCompany_notFound() {
            // given
            UUID companyId = UUID.randomUUID();
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> companyService.getCompany(companyId))
                    .isInstanceOf(CompanyNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("업체 전체 조회")
    class GetAllCompanies {

        @Test
        @DisplayName("검색어를 정규화하고 허용되지 않은 페이지 크기와 정렬 필드는 기본값으로 보정")
        void getAllCompanies_normalizeSearchOptions() {
            // given
            UUID companyId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            Company company = company(companyId, "모니터 업체", CompanyType.PRODUCER, hubId, "서울시 중구");

            given(companyRepository.searchCompanies(eq("모니터"), any(Pageable.class)))
                    .willAnswer(invocation -> {
                        Pageable pageable = invocation.getArgument(1);
                        return new PageImpl<>(List.of(company), pageable, 1);
                    });

            // when
            PageResponse<CompanyResponseDto> response = companyService.getAllCompanies("  모니터  ", -1, 99, "id,asc");

            // then
            assertThat(response.content()).hasSize(1);
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.sort()).isEqualTo("createdAt, ASC");

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(companyRepository).searchCompanies(eq("모니터"), captor.capture());
            Pageable pageable = captor.getValue();
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
            assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.ASC);
        }
    }

    @Nested
    @DisplayName("업체 수정")
    class UpdateCompany {

        @Test
        @DisplayName("업체 수정 성공")
        void updateCompany_success() {
            // given
            UUID companyId = UUID.randomUUID();
            UUID oldHubId = UUID.randomUUID();
            UUID newHubId = UUID.randomUUID();
            Company company = company(companyId, "기존 업체", CompanyType.PRODUCER, oldHubId, "기존 주소");
            CompanyUpdateRequestDto request = updateRequest("수정 업체", CompanyType.RECEIVER, newHubId, "수정 주소");

            givenCurrentUser(UserRole.MASTER, null, null);
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            givenHubExists(newHubId);

            // when
            CompanyResponseDto response = companyService.updateCompany(userId, userRole, companyId, request);

            // then
            assertThat(response.id()).isEqualTo(companyId);
            assertThat(response.companyName()).isEqualTo("수정 업체");
            assertThat(response.companyType()).isEqualTo(CompanyType.RECEIVER);
            assertThat(response.hubId()).isEqualTo(newHubId);
            assertThat(response.address()).isEqualTo("수정 주소");
        }

        @Test
        @DisplayName("업체 수정 실패 - 존재하지 않는 업체")
        void updateCompany_companyNotFound() {
            // given
            UUID companyId = UUID.randomUUID();
            CompanyUpdateRequestDto request = updateRequest("수정 업체", CompanyType.RECEIVER, UUID.randomUUID(), "수정 주소");

            givenCurrentUser(UserRole.MASTER, null, null);
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> companyService.updateCompany(userId, userRole, companyId, request))
                    .isInstanceOf(CompanyNotFoundException.class);

            verifyNoInteractions(hubClient);
        }

        @Test
        @DisplayName("업체 수정 실패 - 존재하지 않는 허브")
        void updateCompany_hubNotFound() {
            // given
            UUID companyId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            Company company = company(companyId, "기존 업체", CompanyType.PRODUCER, UUID.randomUUID(), "기존 주소");
            CompanyUpdateRequestDto request = updateRequest("수정 업체", CompanyType.RECEIVER, hubId, "수정 주소");

            givenCurrentUser(UserRole.MASTER, null, null);
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));
            given(hubClient.getHub(userId, userRole, hubId)).willThrow(feignException(404));

            // when & then
            assertThatThrownBy(() -> companyService.updateCompany(userId, userRole, companyId, request))
                    .isInstanceOf(CompanyHubNotFoundException.class);
        }

        @Test
        @DisplayName("업체 수정 실패 - COMPANY_MANAGER 본인 업체 불일치")
        void updateCompany_companyManagerDifferentCompanyDenied() {
            // given
            UUID companyId = UUID.randomUUID();
            UUID userCompanyId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            Company company = company(companyId, "기존 업체", CompanyType.PRODUCER, hubId, "기존 주소");
            CompanyUpdateRequestDto request = updateRequest("수정 업체", CompanyType.RECEIVER, hubId, "수정 주소");

            givenCurrentUser(UserRole.COMPANY_MANAGER, null, userCompanyId);
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));

            // when & then
            assertThatThrownBy(() -> companyService.updateCompany(userId, UserRole.COMPANY_MANAGER, companyId, request))
                    .isInstanceOf(CompanyAccessDeniedException.class);

            verifyNoInteractions(hubClient);
        }
    }

    @Nested
    @DisplayName("업체 삭제")
    class DeleteCompany {

        @Test
        @DisplayName("업체 삭제 성공 (soft delete)")
        void deleteCompany_success() {
            // given
            UUID companyId = UUID.randomUUID();
            Company company = company(companyId, "모니터 업체", CompanyType.PRODUCER, UUID.randomUUID(), "서울시 중구");

            givenCurrentUser(UserRole.MASTER, null, null);
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));

            // when
            companyService.deleteCompany(userId, userRole, companyId);

            // then
            assertThat(company.isDeleted()).isTrue();
            assertThat(company.getDeletedBy()).isEqualTo(userId.toString());
        }

        @Test
        @DisplayName("업체 삭제 실패 - 존재하지 않는 업체")
        void deleteCompany_companyNotFound() {
            // given
            UUID companyId = UUID.randomUUID();
            givenCurrentUser(UserRole.MASTER, null, null);
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> companyService.deleteCompany(userId, userRole, companyId))
                    .isInstanceOf(CompanyNotFoundException.class);
        }

        @Test
        @DisplayName("업체 삭제 실패 - HUB_MANAGER 담당 허브 불일치")
        void deleteCompany_hubManagerDifferentHubDenied() {
            // given
            UUID companyId = UUID.randomUUID();
            UUID companyHubId = UUID.randomUUID();
            UUID userHubId = UUID.randomUUID();
            Company company = company(companyId, "모니터 업체", CompanyType.PRODUCER, companyHubId, "서울시 중구");

            givenCurrentUser(UserRole.HUB_MANAGER, userHubId, null);
            given(companyRepository.findByIdAndDeletedAtIsNull(companyId)).willReturn(Optional.of(company));

            // when & then
            assertThatThrownBy(() -> companyService.deleteCompany(userId, UserRole.HUB_MANAGER, companyId))
                    .isInstanceOf(CompanyAccessDeniedException.class);

            assertThat(company.isDeleted()).isFalse();
        }
    }

    private CompanyCreateRequestDto createRequest(String name, CompanyType type, UUID hubId, String address) {
        return new CompanyCreateRequestDto(name, type, hubId, address);
    }

    private CompanyUpdateRequestDto updateRequest(String name, CompanyType type, UUID hubId, String address) {
        return new CompanyUpdateRequestDto(name, type, hubId, address);
    }

    private Company company(UUID id, String name, CompanyType type, UUID hubId, String address) {
        Company company = Company.builder()
                .companyName(name)
                .companyType(type)
                .hubId(hubId)
                .address(address)
                .build();
        setId(company, id);
        return company;
    }

    private void givenHubExists(UUID hubId) {
        HubResponse response = new HubResponse(
                hubId,
                "서울특별시 센터",
                "서울시 중구",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );
        given(hubClient.getHub(userId, userRole, hubId)).willReturn(ApiResponse.ok(response));
    }

    private void givenCurrentUser(UserRole role, UUID hubId, UUID companyId) {
        UserResponse response = new UserResponse(
                userId,
                "testuser",
                "slack",
                role,
                "APPROVED",
                companyId,
                hubId
        );
        given(userAuthorizationValidator.validateCurrentUser(userId, role)).willReturn(response);
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/v1/hubs/" + UUID.randomUUID(),
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        Response response = Response.builder()
                .status(status)
                .reason("error")
                .request(request)
                .headers(Map.of())
                .build();

        return FeignException.errorStatus("HubClient#getHub", response);
    }

    private void setId(Company company, UUID id) {
        try {
            Field field = Company.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(company, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
