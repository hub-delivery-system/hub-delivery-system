package com.hubdelivery.gatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange,
		GatewayFilterChain chain) {

		String path = exchange.getRequest().getURI().getPath();
		String method = exchange.getRequest().getMethod().name();

		String authorization =
			exchange.getRequest()
				.getHeaders()
				.getFirst("Authorization");

		log.info("[Gateway Request] {} {}", method, path);

		if (authorization != null) {
			log.info("[Authorization Header Exists]");
		}

		long startTime = System.currentTimeMillis();

		return chain.filter(exchange)
			.then(Mono.fromRunnable(() -> {

				long endTime = System.currentTimeMillis();

				int statusCode =
					exchange.getResponse()
						.getStatusCode()
						.value();

				log.info(
					"[Gateway Response] status={} duration={}ms",
					statusCode,
					(endTime - startTime)
				);
			}));
	}

	@Override
	public int getOrder() {
		return -2;
	}
}
