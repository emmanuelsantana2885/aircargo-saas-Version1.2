package com.aircargo.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        String email = request.getHeaders().getFirst("X-User-Email");
        String role = request.getHeaders().getFirst("X-User-Role");
        String requestId = request.getHeaders().getFirst(SecurityHeadersFilter.REQUEST_ID_HEADER);
        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;
            // Correlate this access line with the request id injected by
            // SecurityHeadersFilter (read from the response side since the
            // mutated request may not be visible here on every route).
            String rid = exchange.getResponse().getHeaders().getFirst(SecurityHeadersFilter.REQUEST_ID_HEADER);
            if (rid == null) {
                rid = requestId != null ? requestId : "-";
            }
            try (MDC.MDCCloseable ignored = MDC.putCloseable("requestId", rid)) {
                log.info("{} {} {} [{}] user={} role={} {}ms reqId={}",
                        method, path, statusCode,
                        exchange.getResponse().getStatusCode(),
                        email != null ? email : "anonymous",
                        role != null ? role : "-",
                        duration, rid);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
