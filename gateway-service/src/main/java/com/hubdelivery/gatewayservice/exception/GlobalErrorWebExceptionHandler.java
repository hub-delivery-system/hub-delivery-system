package com.hubdelivery.gatewayservice.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
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

    private final GatewayErrorWriter errorWriter;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        GatewayErrorCode errorCode;

        if (ex instanceof ResponseStatusException rse) {
            errorCode = switch (rse.getStatusCode().value()) {
                case 404 -> GatewayErrorCode.NOT_FOUND;
                case 503 -> GatewayErrorCode.SERVICE_UNAVAILABLE;
                default -> GatewayErrorCode.GATEWAY_ERROR;
            };
        } else {
            log.error("Unhandled exception in gateway", ex);
            errorCode = GatewayErrorCode.INTERNAL_SERVER_ERROR;
        }

        return errorWriter.write(exchange, errorCode);
    }
}
