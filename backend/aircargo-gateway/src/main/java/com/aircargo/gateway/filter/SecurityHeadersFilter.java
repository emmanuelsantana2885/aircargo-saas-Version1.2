package com.aircargo.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Security hardening + request correlation for the gateway.
 *
 * Adds the OWASP-baseline security headers recommended for any production
 * web API (CSP, HSTS, X-Frame-Options, X-Content-Type-Options,
 * Referrer-Policy) and injects/propagates an X-Request-Id header so the
 * AccessLogFilter and downstream services can correlate a single request
 * end to end.
 */
@Component
public class SecurityHeadersFilter implements GlobalFilter, Ordered {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_ATTR = SecurityHeadersFilter.class.getName() + ".requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }

        ServerHttpRequest mutated = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .header("Content-Security-Policy",
                        "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
                        + "img-src 'self' data:; font-src 'self' data:; object-src 'none'; "
                        + "frame-ancestors 'none'; base-uri 'self'; form-action 'self'")
                .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
                .header("X-Frame-Options", "DENY")
                .header("X-Content-Type-Options", "nosniff")
                .header("Referrer-Policy", "strict-origin-when-cross-origin")
                .build();

        exchange.getAttributes().put(REQUEST_ID_ATTR, requestId);

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        // Run before JwtGatewayFilter so the request id is already present
        // when JWT parsing and downstream filters run.
        return -300;
    }
}
