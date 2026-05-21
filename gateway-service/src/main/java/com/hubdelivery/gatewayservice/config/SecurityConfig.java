package com.hubdelivery.gatewayservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubdelivery.gatewayservice.exception.ErrorResponse;
import com.hubdelivery.gatewayservice.exception.GatewayErrorCode;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ObjectMapper objectMapper;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
			.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
			.cors(cors -> cors.configurationSource(exchange -> {
				CorsConfiguration config = new CorsConfiguration();
				config.setAllowedOriginPatterns(List.of("*"));
				config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
				config.setAllowedHeaders(List.of("*"));
				config.setAllowCredentials(true);
				config.setMaxAge(3600L);
				return config;
			}))
			.authorizeExchange(exchange -> exchange
				.pathMatchers(
					"/api/v1/auth/**",
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/webjars/**",
					"/v3/api-docs/**",
					"/user-service/v3/api-docs",
					"/hub-service/v3/api-docs",
					"/company-product-service/v3/api-docs",
					"/order-service/v3/api-docs",
					"/delivery-service/v3/api-docs",
					"/slack-ai-service/v3/api-docs",
					"/eureka/**"
				).permitAll()
				.anyExchange().authenticated()
			)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((exchange, e) ->
					writeErrorResponse(exchange, GatewayErrorCode.TOKEN_EMPTY))
				.accessDeniedHandler((exchange, e) ->
					writeErrorResponse(exchange, GatewayErrorCode.FORBIDDEN))
			)
			.build();
	}

	private Mono<Void> writeErrorResponse(ServerWebExchange exchange, GatewayErrorCode errorCode) {
		ErrorResponse body = ErrorResponse.of(errorCode);
		exchange.getResponse().setStatusCode(errorCode.getStatus());
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
		try {
			byte[] bytes = objectMapper.writeValueAsBytes(body);
			DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
			return exchange.getResponse().writeWith(Mono.just(buffer));
		} catch (Exception e) {
			return exchange.getResponse().setComplete();
		}
	}
}
