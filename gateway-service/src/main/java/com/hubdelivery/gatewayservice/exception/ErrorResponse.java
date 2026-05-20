package com.hubdelivery.gatewayservice.exception;

import org.springframework.http.HttpStatus;

public record ErrorResponse(int status, String code, String message) {

	public static ErrorResponse of(HttpStatus httpStatus, String code, String message) {
		return new ErrorResponse(httpStatus.value(), code, message);
	}
}
