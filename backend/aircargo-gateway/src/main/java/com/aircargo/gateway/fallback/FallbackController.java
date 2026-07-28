package com.aircargo.gateway.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping("/**")
    public ResponseEntity<Map<String, Object>> fallback(String service) {
        log.warn("Circuit breaker fallback triggered for service: {}", service);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Service temporarily unavailable",
                        "status", 503,
                        "message", "The service is currently experiencing issues. Please try again later.",
                        "service", service != null ? service : "unknown"
                ));
    }
}
