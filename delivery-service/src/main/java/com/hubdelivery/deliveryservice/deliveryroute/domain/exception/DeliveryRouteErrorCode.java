package com.hubdelivery.deliveryservice.deliveryroute.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryRouteErrorCode implements ErrorCode {

    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY-ROUTE-001", "배송 경로를 찾을 수 없습니다."),
    ROUTE_FORBIDDEN(HttpStatus.FORBIDDEN, "DELIVERY-ROUTE-002", "접근 권한이 없습니다."),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY-ROUTE-003", "해당 배송을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
