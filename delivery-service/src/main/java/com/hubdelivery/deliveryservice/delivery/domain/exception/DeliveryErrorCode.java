package com.hubdelivery.deliveryservice.delivery.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {

    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY-001", "배송을 찾을 수 없습니다."),
    DELIVERY_FORBIDDEN(HttpStatus.FORBIDDEN, "DELIVERY-002", "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
