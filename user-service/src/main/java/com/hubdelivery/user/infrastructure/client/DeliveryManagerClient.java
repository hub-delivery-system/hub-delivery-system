package com.hubdelivery.user.infrastructure.client;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.user.domain.exception.DeliveryManagerCapacityExceededException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryManagerClient {

    private static final String SYSTEM_USER_ID = "00000000-0000-0000-0000-000000000000";
    private static final String SYSTEM_ROLE = "MASTER";
    private static final String DELIVERY_MANAGER_CAPACITY_EXCEEDED_CODE = "DELIVERY-MANAGER-005";

    private final DeliveryManagerIntegrationProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public void createHubDeliveryManager(UUID userId, UUID hubId) {
        createDeliveryManager(userId, hubId, null, "HUB_DELIVERY");
    }

    public void createCompanyDeliveryManager(UUID userId, UUID companyId, UUID hubId) {
        createDeliveryManager(userId, hubId, companyId, "COMPANY_DELIVERY");
    }

    private void createDeliveryManager(UUID userId, UUID hubId, UUID companyId, String type) {
        String uri = properties.requiredServiceBaseUrl() + "/api/v1/delivery-managers";

        try {
            restClient.post()
                    .uri(uri)
                    .header("X-User-Id", SYSTEM_USER_ID)
                    .header("X-Role", SYSTEM_ROLE)
                    .body(new DeliveryManagerCreateRequest(userId, hubId, companyId, type))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            DeliveryManagerErrorResponse error = parseError(e.getResponseBodyAsString());
            if (isCapacityExceeded(e.getStatusCode().value(), error)) {
                throw new DeliveryManagerCapacityExceededException(error.message());
            }

            log.error("Delivery-manager 생성 연동 실패. status={}, body={}",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new CommonException(CommonErrorCode.EXTERNAL_API_FAILURE, "delivery-service 연동에 실패했습니다.");
        } catch (RestClientException e) {
            log.error("Delivery-manager 생성 연동 실패.", e);
            throw new CommonException(CommonErrorCode.EXTERNAL_API_FAILURE, "delivery-service 연동에 실패했습니다.");
        }
    }

    private boolean isCapacityExceeded(int status, DeliveryManagerErrorResponse error) {
        return status == 409
                && error != null
                && DELIVERY_MANAGER_CAPACITY_EXCEEDED_CODE.equals(error.code());
    }

    private DeliveryManagerErrorResponse parseError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(responseBody, DeliveryManagerErrorResponse.class);
        } catch (JsonProcessingException ex) {
            log.warn("Delivery-service 에러 응답 파싱 실패. body={}", responseBody);
            return null;
        }
    }

    private record DeliveryManagerCreateRequest(
            UUID userId,
            UUID hubId,
            UUID companyId,
            String type
    ) {
    }

    private record DeliveryManagerErrorResponse(
            Integer status,
            String code,
            String message
    ) {
    }
}
