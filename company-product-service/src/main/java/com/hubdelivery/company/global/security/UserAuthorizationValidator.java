package com.hubdelivery.company.global.security;

import com.hubdelivery.common.exception.CommonErrorCode;
import com.hubdelivery.common.exception.CommonException;
import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.global.infrastructure.client.user.UserClient;
import com.hubdelivery.company.global.infrastructure.client.user.dto.UserResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAuthorizationValidator {

    private static final String APPROVED_STATUS = "APPROVED";

    private final UserClient userClient;

    public UserResponse validateCurrentUser(UUID userId, UserRole requestRole) {
        UserResponse user = getUser(userId, requestRole);

        if (user.role() != requestRole) {
            throw new CommonException(CommonErrorCode.FORBIDDEN, "현재 사용자 권한이 요청 권한과 일치하지 않습니다.");
        }

        if (user.status() == null || !APPROVED_STATUS.equalsIgnoreCase(user.status())) {
            throw new CommonException(CommonErrorCode.FORBIDDEN, "승인된 사용자만 요청할 수 있습니다.");
        }

        return user;
    }

    private UserResponse getUser(UUID userId, UserRole requestRole) {
        try {
            ApiResponse<UserResponse> response = userClient.getUser(userId, requestRole, userId);
            if (response == null || response.data() == null) {
                throw new CommonException(CommonErrorCode.FORBIDDEN, "사용자 정보를 확인할 수 없습니다.");
            }
            return response.data();
        } catch (FeignException.NotFound e) {
            throw new CommonException(CommonErrorCode.FORBIDDEN, "사용자 정보를 확인할 수 없습니다.");
        } catch (FeignException e) {
            throw new CommonException(CommonErrorCode.EXTERNAL_API_FAILURE, "사용자 서비스 연동에 실패했습니다.");
        }
    }
}
