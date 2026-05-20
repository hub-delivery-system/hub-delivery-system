package com.hubdelivery.gatewayservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
@Slf4j
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements WebExceptionHandler {

	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		if (exchange.getResponse().isCommitted()) {
			return Mono.error(ex);
		}

		HttpStatus status;
		String code;
		String message;

		if (ex instanceof ResponseStatusException rse) {
			int value = rse.getStatusCode().value();
			status = HttpStatus.resolve(value) != null ? HttpStatus.resolve(value) : HttpStatus.INTERNAL_SERVER_ERROR;
			code = switch (value) {
				case 404 -> "NOT_FOUND";
				case 503 -> "SERVICE_UNAVAILABLE";
				default -> "GATEWAY_ERROR";
			};
			message = switch (value) {
				case 404 -> "요청한 경로를 찾을 수 없습니다.";
				case 503 -> "서비스에 일시적으로 접근할 수 없습니다.";
				default -> rse.getReason() != null ? rse.getReason() : "게이트웨이 오류가 발생했습니다.";
			};
		} else {
			log.error("Unhandled exception in gateway", ex);
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			code = "INTERNAL_SERVER_ERROR";
			message = "서버 내부 오류가 발생했습니다.";
		}

		return writeResponse(exchange, status, code, message);
	}

	private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, String code, String message) {
		ErrorResponse body = ErrorResponse.of(status, code, message);
		exchange.getResponse().setStatusCode(status);
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
		try {
			byte[] bytes = objectMapper.writeValueAsBytes(body);
			DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
			return exchange.getResponse().writeWith(Mono.just(buffer));
		} catch (Exception e) {
			log.error("Failed to write error response", e);
			return exchange.getResponse().setComplete();
		}
	}
}
