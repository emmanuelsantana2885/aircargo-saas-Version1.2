package com.aircargo.gateway.filter;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityHeadersFilterTest {

    private final SecurityHeadersFilter filter = new SecurityHeadersFilter();

    private ServerWebExchange captureExchange(MockServerWebExchange exchange) {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(chain.filter(captor.capture())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        return captor.getValue();
    }

    @Test
    void addsAllSecurityHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/flights").build());

        ServerWebExchange passed = captureExchange(exchange);
        HttpHeaders headers = passed.getRequest().getHeaders();

        assertEquals("DENY", headers.getFirst("X-Frame-Options"));
        assertEquals("nosniff", headers.getFirst("X-Content-Type-Options"));
        assertEquals("max-age=31536000; includeSubDomains", headers.getFirst("Strict-Transport-Security"));
        assertEquals("strict-origin-when-cross-origin", headers.getFirst("Referrer-Policy"));
        assertNotNull(headers.getFirst("Content-Security-Policy"));
        assertTrue(headers.getFirst("Content-Security-Policy").contains("frame-ancestors 'none'"));
    }

    @Test
    void generatesRequestId_whenAbsent() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/flights").build());

        ServerWebExchange passed = captureExchange(exchange);
        String requestId = passed.getRequest().getHeaders().getFirst(SecurityHeadersFilter.REQUEST_ID_HEADER);

        assertNotNull(requestId);
        assertFalse(requestId.isBlank());
        assertEquals(requestId, exchange.getAttributes().get(SecurityHeadersFilter.REQUEST_ID_ATTR));
    }

    @Test
    void propagatesExistingRequestId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/flights")
                        .header(SecurityHeadersFilter.REQUEST_ID_HEADER, "client-supplied-id")
                        .build());

        ServerWebExchange passed = captureExchange(exchange);

        assertEquals("client-supplied-id",
                passed.getRequest().getHeaders().getFirst(SecurityHeadersFilter.REQUEST_ID_HEADER));
    }
}
