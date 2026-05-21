package com.hubdelivery.gatewayservice.filter;

import com.hubdelivery.gatewayservice.exception.GatewayErrorCode;
import com.hubdelivery.gatewayservice.exception.GatewayErrorWriter;
import com.hubdelivery.gatewayservice.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtProvider jwtProvider;
    private final GatewayErrorWriter errorWriter;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // JWT 검증은 Spring Security(BearerTokenAuthenticationWebFilter)가 이미 수행.
        // 이 필터는 검증된 토큰에서 사용자 정보를 꺼내 하위 서비스용 헤더로 변환하는 역할만 담당.
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth instanceof JwtAuthenticationToken && auth.isAuthenticated())
            .cast(JwtAuthenticationToken.class)
            .flatMap(authToken -> {
                Jwt jwt = authToken.getToken();
                String userId = jwtProvider.extractUserId(jwt);
                String role   = jwtProvider.extractRole(jwt);

                if (userId == null || role == null) {
                    // TODO: Keycloak realm에서 사용자에게 애플리케이션 역할이 할당됐는지 확인 필요
                    log.warn("JWT missing required claims: userId={}, role={}", userId, role);
                    return errorWriter.write(exchange, GatewayErrorCode.TOKEN_INVALID);
                }

                HttpHeaders mutableHeaders = new HttpHeaders();
                mutableHeaders.addAll(exchange.getRequest().getHeaders());
                mutableHeaders.set("X-User-Id", userId);
                mutableHeaders.set("X-Role", role);

                ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                    @Override
                    public HttpHeaders getHeaders() {
                        return mutableHeaders;
                    }
                };

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            })
            // SecurityContext가 없는 경우(permitAll 경로): 헤더 주입 없이 그대로 통과
            .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
