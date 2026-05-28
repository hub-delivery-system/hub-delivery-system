package com.hubdelivery.deliveryservice.deliverymanager.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryManagerErrorCode implements ErrorCode {

    MANAGER_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY-MANAGER-001", "배송 담당자를 찾을 수 없습니다."),
    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY-MANAGER-002", "존재하지 않는 허브입니다."),
    MANAGER_FORBIDDEN(HttpStatus.FORBIDDEN, "DELIVERY-MANAGER-003", "접근 권한이 없습니다."),
    NO_AVAILABLE_MANAGER(HttpStatus.NOT_FOUND, "DELIVERY-MANAGER-004", "배정 가능한 배송 담당자가 없습니다."),
    CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "DELIVERY-MANAGER-005", "배송 담당자 정원이 초과되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
