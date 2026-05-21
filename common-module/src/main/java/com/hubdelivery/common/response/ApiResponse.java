package com.hubdelivery.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hubdelivery.common.exception.ErrorCode;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String code,
        String message,
        T data,
        List<FieldError> errors
) {

    private static final String SUCCESS_MESSAGE = "SUCCESS";
    private static final String CREATED_MESSAGE = "CREATED";

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, null, SUCCESS_MESSAGE, data, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, null, CREATED_MESSAGE, data, null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getMessage());
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message,
                null,
                null
        );
    }

    public static ApiResponse<Void> validationError(ErrorCode errorCode, List<FieldError> errors) {
        return new ApiResponse<>(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                errors
        );
    }

    public record FieldError(String field, String message) {

    }
}
