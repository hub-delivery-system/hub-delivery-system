package com.hubdelivery.user.infrastructure.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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
public class HubDirectoryClient {

    private static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000000";
    private static final String SYSTEM_ROLE = "MASTER";
    private static final int LOOKUP_PAGE_SIZE = 100;

    private static final ParameterizedTypeReference<ApiResponseEnvelope<PageEnvelope<HubSummary>>> HUB_LIST_RESPONSE =
            new ParameterizedTypeReference<>() {
            };

    private final AffiliationLookupProperties properties;
    private final RestClient restClient = RestClient.create();

    @Retryable(
            retryFor = CommonException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 300)
    )
    public Optional<UUID> findHubIdByName(String affiliationName) {
        String normalizedName = normalize(affiliationName);
        if (!StringUtils.hasText(normalizedName)) {
            return Optional.empty();
        }

        String uri = UriComponentsBuilder.fromHttpUrl(properties.requiredHubServiceBaseUrl())
                .path("/api/v1/hubs")
                .queryParam("keyword", normalizedName)
                .queryParam("page", 0)
                .queryParam("size", LOOKUP_PAGE_SIZE)
                .build()
                .toUriString();

        try {
            ApiResponseEnvelope<PageEnvelope<HubSummary>> response = restClient.get()
                    .uri(uri)
                    .header("X-User-Id", SYSTEM_USER_ID)
                    .header("X-Role", SYSTEM_ROLE)
                    .retrieve()
                    .body(HUB_LIST_RESPONSE);

            if (response == null || response.data() == null || response.data().content() == null) {
                return Optional.empty();
            }

            return response.data().content().stream()
                    .filter(hub -> normalizedName.equals(normalize(hub.hubName())))
                    .map(HubSummary::hubId)
                    .findFirst();
        } catch (RestClientResponseException e) {
            log.error("Hub 조회 연동 실패. status={}, body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new CommonException(CommonErrorCode.EXTERNAL_API_FAILURE, "hub-service 조회에 실패했습니다.");
        } catch (RestClientException e) {
            log.error("Hub 조회 연동 실패.", e);
            throw new CommonException(CommonErrorCode.EXTERNAL_API_FAILURE, "hub-service 조회에 실패했습니다.");
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

    private record HubSummary(
            UUID hubId,
            String hubName
    ) {
    }
}
