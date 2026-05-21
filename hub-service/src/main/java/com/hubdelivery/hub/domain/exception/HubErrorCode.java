package com.hubdelivery.hub.domain.exception;

import com.hubdelivery.common.exception.CommonErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HubErrorCode implements CommonErrorCode {
    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "HUB-404", "허브를 찾을 수 없습니다."),
    HUB_DUPLICATE_LOCATION(HttpStatus.CONFLICT, "HUB-409", "이미 존재하는 위치입니다."),
    HUB_DUPLICATE_NAME(HttpStatus.CONFLICT, "HUB-410", "이미 존재하는 허브 이름입니다."),
    HUB_INVALID_COORDINATES(HttpStatus.BAD_REQUEST, "HUB-400", "유효하지 않은 위도/경도입니다."),
    HUB_ADDRESS_INVALID(HttpStatus.BAD_REQUEST, "HUB-401", "유효하지 않은 주소입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
