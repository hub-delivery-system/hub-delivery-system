package com.hubdelivery.common.exception;

import com.hubdelivery.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("비즈니스 예외가 발생했습니다. code={}, message={}", errorCode.name(), e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

        List<ApiResponse.FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        log.warn("요청 본문 검증에 실패했습니다. errors={}", errors);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.validationError(errorCode.getMessage(), errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        log.warn("접근 권한이 없는 요청입니다. message={}", e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        log.warn("인증되지 않은 요청입니다. message={}", e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        log.warn("요청한 리소스를 찾을 수 없습니다. path={}", e.getResourcePath());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("처리되지 않은 서버 예외가 발생했습니다.", e);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }
}
