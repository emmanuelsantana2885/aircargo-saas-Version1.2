package com.aircargo.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("auth-service", r -> r
                        .path("/api/auth/**", "/api/users/**", "/api/audit-logs/**",
                             "/api/sites/**", "/api/role-permissions/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("auth-service")
                                        .setFallbackUri("forward:/fallback/auth"))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(java.time.Duration.ofMillis(500),
                                                     java.time.Duration.ofSeconds(2), 2, true))
                        )
                        .uri("http://localhost:9092"))

                .route("flight-service", r -> r
                        .path("/api/flights/**", "/api/airlines/**", "/api/aircraft-types/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("flight-service")
                                        .setFallbackUri("forward:/fallback/flight"))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(java.time.Duration.ofMillis(500),
                                                     java.time.Duration.ofSeconds(2), 2, true))
                        )
                        .uri("http://localhost:9093"))

                .route("booking-service", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("booking-service")
                                        .setFallbackUri("forward:/fallback/booking"))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(java.time.Duration.ofMillis(500),
                                                     java.time.Duration.ofSeconds(2), 2, true))
                        )
                        .uri("http://localhost:9094"))

                .route("mawb-service", r -> r
                        .path("/api/cargo/mawbs/**", "/api/cargo/hawbs/**",
                             "/api/tracking/**", "/api/mawbs/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("mawb-service")
                                        .setFallbackUri("forward:/fallback/mawb"))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(java.time.Duration.ofMillis(500),
                                                     java.time.Duration.ofSeconds(2), 2, true))
                        )
                        .uri("http://localhost:9095"))

                .route("warehouse-service", r -> r
                        .path("/api/warehouse/**", "/api/receipts/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("warehouse-service")
                                        .setFallbackUri("forward:/fallback/warehouse"))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(java.time.Duration.ofMillis(500),
                                                     java.time.Duration.ofSeconds(2), 2, true))
                        )
                        .uri("http://localhost:9096"))

                .route("uld-service", r -> r
                        .path("/api/ulds/**", "/api/uld-awbs/**",
                             "/api/uld-type-config/**", "/api/scan/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("uld-service")
                                        .setFallbackUri("forward:/fallback/uld"))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(java.time.Duration.ofMillis(500),
                                                     java.time.Duration.ofSeconds(2), 2, true))
                        )
                        .uri("http://localhost:9097"))

                .route("load-planning-service", r -> r
                        .path("/api/load-planning/**", "/api/cargo/flights/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("load-planning-service")
                                        .setFallbackUri("forward:/fallback/load-planning"))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(java.time.Duration.ofMillis(500),
                                                     java.time.Duration.ofSeconds(2), 2, true))
                        )
                        .uri("http://localhost:9098"))

                .route("export-service", r -> r
                        .path("/api/exports/**", "/api/bi/**", "/api/reports/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("export-service")
                                        .setFallbackUri("forward:/fallback/export"))
                                .retry(config -> config
                                        .setRetries(2)
                                        .setBackoff(java.time.Duration.ofMillis(500),
                                                     java.time.Duration.ofSeconds(2), 2, true))
                        )
                        .uri("http://localhost:9099"))

                .route("compliance-service", r -> r
                        .path("/api/compliance/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("compliance-service")
                                        .setFallbackUri("forward:/fallback/compliance"))
                        )
                        .uri("http://localhost:9091"))

                .route("api-fallback", r -> r
                        .path("/api/**")
                        .filters(f -> f
                                .circuitBreaker(config -> config
                                        .setName("default")
                                        .setFallbackUri("forward:/fallback/default"))
                        )
                        .uri("http://localhost:9091"))

                .build();
    }
}
