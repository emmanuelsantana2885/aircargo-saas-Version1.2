package com.aircargo.gateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final Map<String, RateLimiter> userLimiters = new ConcurrentHashMap<>();

    private final boolean enabled;
    private final int limitPerMinute;
    private final long timeoutMs;

    public RateLimitFilter(
            @Value("${app.gateway.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.gateway.rate-limit.limit-per-minute:100}") int limitPerMinute,
            @Value("${app.gateway.rate-limit.timeout-ms:50}") long timeoutMs) {
        this.enabled = enabled;
        this.limitPerMinute = limitPerMinute;
        this.timeoutMs = timeoutMs;
    }

    private RateLimiterConfig defaultConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(limitPerMinute)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofMillis(timeoutMs))
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        String email = request.getHeaders().getFirst("X-User-Email");
        if (email == null || email.isBlank()) {
            return chain.filter(exchange);
        }

        RateLimiter rateLimiter = userLimiters.computeIfAbsent(email, k ->
                RateLimiter.of("rl_" + k, defaultConfig()));

        if (rateLimiter.acquirePermission()) {
            return chain.filter(exchange);
        }

        log.warn("Rate limit exceeded for user: {}", email);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Retry-After", "60");
        String body = "{\"error\":\"Rate limit exceeded. Try again later.\",\"status\":429}";
        byte[] bytes = body.getBytes();
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -90;
    }
}
