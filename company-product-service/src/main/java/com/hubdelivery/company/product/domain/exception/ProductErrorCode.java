package com.hubdelivery.company.product.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-001", "상품 업체를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-002", "상품을 찾을 수 없습니다."),
    RELATED_HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-003", "상품 관리 허브를 찾을 수 없습니다."),

    RELATED_HUB_INTEGRATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "PRODUCT-901", "허브 서비스 연동에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
