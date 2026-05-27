package com.hubdelivery.hubtohub.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum HubToHubErrorCode implements ErrorCode {

    HUBTOHUB_NOT_FOUND(HttpStatus.NOT_FOUND, "HUBTOHUB-001", "허브 경로를 찾을 수 없습니다."),
    HUBTOHUB_DUPLICATE_LOCATION(HttpStatus.CONFLICT, "HUBTOHUB-002", "이미 존재하는 경로입니다."),
    HUBTOHUB_INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "HUBTOHUB-003", "유효하지 않은 허브입니다."),

    // ⭐ 외부 API 에러 (카카오)
    KAKAO_API_FAILED(HttpStatus.BAD_GATEWAY, "HUBTOHUB-101", "경로 정보 조회에 실패했습니다."),
    KAKAO_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "HUBTOHUB-102", "해당 경로를 찾을 수 없습니다."),
    KAKAO_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "HUBTOHUB-103", "경로 조회 응답 시간이 초과되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
