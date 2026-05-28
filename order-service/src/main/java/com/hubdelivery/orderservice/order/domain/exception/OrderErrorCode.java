package com.hubdelivery.orderservice.order.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-001", "주문을 찾을 수 없습니다."),
    ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "ORDER-002", "접근 권한이 없습니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "ORDER-003", "허용되지 않는 상태 전이입니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "ORDER-004", "재고가 부족합니다."),
    DELIVERY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER-005", "배송 생성에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
