package com.hubdelivery.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.user.domain.entity.User;
import com.hubdelivery.user.infrastructure.client.CompanyDirectoryClient;
import com.hubdelivery.user.infrastructure.client.DeliveryManagerClient;

@Service
@RequiredArgsConstructor
public class DeliveryManagerProvisionService {

    private final DeliveryManagerClient deliveryManagerClient;
    private final CompanyDirectoryClient companyDirectoryClient;

    public void provisionIfRequired(User user) {
        RequestedRole requestedRole = user.getRequestedRole();

        if (requestedRole == RequestedRole.HUB_DELIVERY_MANAGER) {
            UUID hubId = requireValue(user.getHubId(), "허브 배송 담당자 승인에는 hubId가 필요합니다.");
            deliveryManagerClient.createHubDeliveryManager(user.getId(), hubId);
            return;
        }

        if (requestedRole == RequestedRole.COMPANY_DELIVERY_MANAGER) {
            UUID companyId = requireValue(user.getCompanyId(), "업체 배송 담당자 승인에는 companyId가 필요합니다.");
            UUID hubId = companyDirectoryClient.findHubIdByCompanyId(companyId)
                    .orElseThrow(() -> new CommonException(
                            CommonErrorCode.RESOURCE_NOT_FOUND,
                            "업체의 기준 허브를 찾을 수 없습니다. companyId=" + companyId
                    ));

            deliveryManagerClient.createCompanyDeliveryManager(user.getId(), companyId, hubId);
        }
    }

    private UUID requireValue(UUID value, String message) {
        if (value == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, message);
        }
        return value;
    }
}
