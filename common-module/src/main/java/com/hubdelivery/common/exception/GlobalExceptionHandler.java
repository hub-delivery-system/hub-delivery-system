package com.hubdelivery.common.exception;

import com.hubdelivery.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 모든 비즈니스 예외 */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[{}] 비즈니스 예외가 발생했습니다. message={}", errorCode.getCode(), e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode, e.getMessage()));
    }

    /** 요청 본문 검증 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ApiResponse.FieldError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ApiResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        log.warn("[{}] 요청 본문 검증에 실패했습니다. errors={}", CommonErrorCode.VALIDATION_ERROR.getCode(), errors);

        return ResponseEntity
                .status(CommonErrorCode.VALIDATION_ERROR.getStatus())
                .body(ApiResponse.validationError(CommonErrorCode.VALIDATION_ERROR, errors));
    }

    /** JSON 파싱 실패 / enum 값 범위 초과 등 — 잘못된 요청 본문 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Throwable cause = e.getMostSpecificCause();
        String causeType = (cause == null ? e : cause).getClass().getSimpleName();
        log.warn("[{}] 요청 본문을 읽을 수 없습니다. cause={}", CommonErrorCode.INVALID_INPUT_VALUE.getCode(), causeType);

        return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ApiResponse.error(CommonErrorCode.INVALID_INPUT_VALUE));
    }

    /** 지원하지 않는 HTTP 메서드 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("[{}] 허용되지 않는 HTTP 메서드입니다. method={}", CommonErrorCode.METHOD_NOT_ALLOWED.getCode(), e.getMethod());

        return ResponseEntity
                .status(CommonErrorCode.METHOD_NOT_ALLOWED.getStatus())
                .body(ApiResponse.error(CommonErrorCode.METHOD_NOT_ALLOWED));
    }

    /** Spring Security 인증 실패 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("[{}] 인증되지 않은 요청입니다. message={}", CommonErrorCode.UNAUTHORIZED.getCode(), e.getMessage());

        return ResponseEntity
                .status(CommonErrorCode.UNAUTHORIZED.getStatus())
                .body(ApiResponse.error(CommonErrorCode.UNAUTHORIZED));
    }

    /** Spring Security 권한 부족 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("[{}] 접근 권한이 없는 요청입니다. message={}", CommonErrorCode.FORBIDDEN.getCode(), e.getMessage());

        return ResponseEntity
                .status(CommonErrorCode.FORBIDDEN.getStatus())
                .body(ApiResponse.error(CommonErrorCode.FORBIDDEN));
    }

    /** 매핑되지 않은 정적/리소스 경로 요청 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("[{}] 요청한 리소스를 찾을 수 없습니다. path={}",
                CommonErrorCode.RESOURCE_NOT_FOUND.getCode(), e.getResourcePath());

        return ResponseEntity
                .status(CommonErrorCode.RESOURCE_NOT_FOUND.getStatus())
                .body(ApiResponse.error(CommonErrorCode.RESOURCE_NOT_FOUND));
    }

    /** 처리되지 않은 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[{}] 처리되지 않은 서버 예외가 발생했습니다.", CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(), e);

        return ResponseEntity
                .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
}
