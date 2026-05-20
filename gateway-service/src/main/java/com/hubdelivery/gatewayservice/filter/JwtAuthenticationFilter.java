package com.hubdelivery.gatewayservice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubdelivery.gatewayservice.exception.ErrorResponse;
import com.hubdelivery.gatewayservice.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	private final JwtProvider jwtProvider;
	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String authHeader = exchange.getRequest()
			.getHeaders()
			.getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return chain.filter(exchange);
		}

		String token = authHeader.substring(7);

		try {
			Claims claims = jwtProvider.validateToken(token);

			String userId = claims.getSubject();
			String role = claims.get("role", String.class);

			if (userId == null || role == null) {
				return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "유효하지 않은 토큰입니다.");
			}

			ServerHttpRequest mutatedRequest = exchange.getRequest()
				.mutate()
				.header("X-User-Id", userId)
				.header("X-Role", role)
				.build();

			return chain.filter(exchange.mutate().request(mutatedRequest).build());

		} catch (ExpiredJwtException e) {
			log.warn("Expired JWT: {}", e.getMessage());
			return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "만료된 토큰입니다.");
		} catch (SecurityException | MalformedJwtException e) {
			log.warn("Invalid JWT: {}", e.getMessage());
			return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "유효하지 않은 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			log.warn("Unsupported JWT: {}", e.getMessage());
			return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_UNSUPPORTED", "지원하지 않는 토큰입니다.");
		} catch (IllegalArgumentException e) {
			log.warn("Empty JWT: {}", e.getMessage());
			return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_MISSING", "토큰이 없습니다.");
		}
	}

	@Override
	public int getOrder() {
		return -1;
	}

	private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String code, String message) {
		ErrorResponse body = ErrorResponse.of(status, code, message);
		exchange.getResponse().setStatusCode(status);
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
		try {
			byte[] bytes = objectMapper.writeValueAsBytes(body);
			DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
			return exchange.getResponse().writeWith(Mono.just(buffer));
		} catch (Exception e) {
			log.error("Failed to write JWT error response", e);
			return exchange.getResponse().setComplete();
		}
	}
}
