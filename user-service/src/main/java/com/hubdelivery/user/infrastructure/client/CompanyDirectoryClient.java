package com.hubdelivery.user.infrastructure.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyDirectoryClient {

    private static final int LOOKUP_PAGE_SIZE = 100;

    private static final ParameterizedTypeReference<ApiResponseEnvelope<PageEnvelope<CompanySummary>>> COMPANY_LIST_RESPONSE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponseEnvelope<CompanyDetail>> COMPANY_DETAIL_RESPONSE =
            new ParameterizedTypeReference<>() {
            };

    private final AffiliationLookupProperties properties;
    private final RestClient restClient = RestClient.create();

    public Optional<UUID> findCompanyIdByName(String affiliationName) {
        String normalizedName = normalize(affiliationName);
        if (!StringUtils.hasText(normalizedName)) {
            return Optional.empty();
        }

        String uri = UriComponentsBuilder.fromHttpUrl(properties.requiredCompanyServiceBaseUrl())
                .path("/api/v1/companies")
                .queryParam("keyword", normalizedName)
                .queryParam("page", 0)
                .queryParam("size", LOOKUP_PAGE_SIZE)
                .build()
                .toUriString();

        try {
            ApiResponseEnvelope<PageEnvelope<CompanySummary>> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(COMPANY_LIST_RESPONSE);

            if (response == null || response.data() == null || response.data().content() == null) {
                return Optional.empty();
            }

            return response.data().content().stream()
                    .filter(company -> normalizedName.equals(normalize(company.companyName())))
                    .map(CompanySummary::id)
                    .findFirst();
        } catch (RestClientResponseException e) {
            log.error("Company 조회 연동 실패. status={}, body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new CommonException(CommonErrorCode.EXTERNAL_API_FAILURE, "company-product-service 조회에 실패했습니다.");
        } catch (RestClientException e) {
            log.error("Company 조회 연동 실패.", e);
            throw new CommonException(CommonErrorCode.EXTERNAL_API_FAILURE, "company-product-service 조회에 실패했습니다.");
        }
    }

    public Optional<UUID> findHubIdByCompanyId(UUID companyId) {
        if (companyId == null) {
            return Optional.empty();
        }

        String uri = UriComponentsBuilder.fromHttpUrl(properties.requiredCompanyServiceBaseUrl())
                .path("/api/v1/companies/{companyId}")
                .buildAndExpand(companyId)
                .toUriString();

        try {
            ApiResponseEnvelope<CompanyDetail> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(COMPANY_DETAIL_RESPONSE);

            if (response == null || response.data() == null) {
                return Optional.empty();
            }

            return Optional.ofNullable(response.data().hubId());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }

            log.error("Company 상세 조회 연동 실패. status={}, body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new CommonException(CommonErrorCode.EXTERNAL_API_FAILURE, "company-product-service 조회에 실패했습니다.");
        } catch (RestClientException e) {
            log.error("Company 상세 조회 연동 실패.", e);
            throw new CommonException(CommonErrorCode.EXTERNAL_API_FAILURE, "company-product-service 조회에 실패했습니다.");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private record ApiResponseEnvelope<T>(
            Integer status,
            String code,
            String message,
            T data
    ) {
    }

    private record PageEnvelope<T>(
            List<T> content
    ) {
    }

    private record CompanySummary(
            UUID id,
            String companyName
    ) {
    }

    private record CompanyDetail(
            UUID id,
            UUID hubId
    ) {
    }
}
