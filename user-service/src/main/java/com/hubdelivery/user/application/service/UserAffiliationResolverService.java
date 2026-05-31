package com.hubdelivery.user.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import com.hubdelivery.auth.domain.type.RequestedRole;
import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.user.infrastructure.client.CompanyDirectoryClient;
import com.hubdelivery.user.infrastructure.client.HubDirectoryClient;

@Service
@RequiredArgsConstructor
public class UserAffiliationResolverService {

    private final HubDirectoryClient hubDirectoryClient;
    private final CompanyDirectoryClient companyDirectoryClient;

    public ResolvedOrganization resolve(RequestedRole requestedRole, String affiliationName) {
        if (requestedRole == null) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "requestedRole은 필수입니다.");
        }
        if (!StringUtils.hasText(affiliationName)) {
            throw new CommonException(CommonErrorCode.INVALID_INPUT_VALUE, "affiliationName은 필수입니다.");
        }

        return switch (requestedRole) {
            case HUB_MANAGER, HUB_DELIVERY_MANAGER -> resolveHub(affiliationName);
            case COMPANY_MANAGER, COMPANY_DELIVERY_MANAGER -> resolveCompany(affiliationName);
        };
    }

    private ResolvedOrganization resolveHub(String affiliationName) {
        UUID hubId = hubDirectoryClient.findHubIdByName(affiliationName)
                .orElseThrow(() -> new CommonException(
                        CommonErrorCode.RESOURCE_NOT_FOUND,
                        "해당 이름의 허브를 찾을 수 없습니다. affiliationName=" + affiliationName
                ));
        return new ResolvedOrganization(hubId, null);
    }

    private ResolvedOrganization resolveCompany(String affiliationName) {
        UUID companyId = companyDirectoryClient.findCompanyIdByName(affiliationName)
                .orElseThrow(() -> new CommonException(
                        CommonErrorCode.RESOURCE_NOT_FOUND,
                        "해당 이름의 업체를 찾을 수 없습니다. affiliationName=" + affiliationName
                ));
        return new ResolvedOrganization(null, companyId);
    }

    public record ResolvedOrganization(
            UUID hubId,
            UUID companyId
    ) {
    }
}
