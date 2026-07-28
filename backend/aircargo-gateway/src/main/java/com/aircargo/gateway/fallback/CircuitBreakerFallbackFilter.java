package com.aircargo.gateway.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CircuitBreakerFallbackFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerFallbackFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).onErrorResume(throwable -> {
            log.error("Service unavailable: {}", throwable.getMessage());
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            response.getHeaders().add("Content-Type", "application/json");
            String body = "{\"error\":\"Service temporarily unavailable. Please try again later.\",\"status\":503}";
            byte[] bytes = body.getBytes();
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        });
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
