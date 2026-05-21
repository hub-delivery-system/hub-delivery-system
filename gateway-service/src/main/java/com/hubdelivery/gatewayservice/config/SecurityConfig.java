package com.hubdelivery.gatewayservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.ServerWebExchange;

import com.hubdelivery.gatewayservice.exception.GatewayErrorCode;
import com.hubdelivery.gatewayservice.exception.GatewayErrorWriter;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final GatewayErrorWriter errorWriter;

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
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(Customizer.withDefaults())
				// 유효하지 않은 토큰(만료, 서명 오류 등)에 대한 응답
				.authenticationEntryPoint((exchange, e) -> resolveTokenError(exchange, e))
			)
			.exceptionHandling(ex -> ex
				// 토큰 자체가 없는 경우
				.authenticationEntryPoint((exchange, e) ->
					errorWriter.write(exchange, GatewayErrorCode.TOKEN_EMPTY))
				.accessDeniedHandler((exchange, e) ->
					errorWriter.write(exchange, GatewayErrorCode.FORBIDDEN))
			)
			.build();
	}

	private Mono<Void> resolveTokenError(ServerWebExchange exchange, AuthenticationException e) {
		if (e instanceof OAuth2AuthenticationException oae) {
			String desc = oae.getError().getDescription();
			// Spring Security 버전 업그레이드 시 에러 description 포맷 변경 여부 확인 필요
			if (desc != null && desc.contains("Jwt expired")) {
				return errorWriter.write(exchange, GatewayErrorCode.TOKEN_EXPIRED);
			}
		}
		return errorWriter.write(exchange, GatewayErrorCode.TOKEN_INVALID);
	}
}
