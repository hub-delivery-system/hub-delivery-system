package com.hubdelivery.gatewayservice.filter;

import com.hubdelivery.gatewayservice.exception.GatewayErrorCode;
import com.hubdelivery.gatewayservice.exception.GatewayErrorWriter;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
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

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT: {}", e.getMessage());
            return errorWriter.write(exchange, GatewayErrorCode.TOKEN_EXPIRED);
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return errorWriter.write(exchange, GatewayErrorCode.TOKEN_INVALID);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT: {}", e.getMessage());
            return errorWriter.write(exchange, GatewayErrorCode.TOKEN_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            log.warn("Empty JWT claims: {}", e.getMessage());
            return errorWriter.write(exchange, GatewayErrorCode.TOKEN_EMPTY);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
