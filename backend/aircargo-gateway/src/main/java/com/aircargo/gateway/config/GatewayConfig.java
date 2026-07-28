package com.aircargo.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GatewayConfig {

    @Bean
    public CircuitBreakerConfigCustomizer defaultCircuitBreaker() {
        return CircuitBreakerConfigCustomizer.of("default", builder -> builder
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .recordExceptions(
                        java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class,
                        org.springframework.web.reactive.function.client.WebClientResponseException.class
                )
                .ignoreExceptions(
                        com.aircargo.gateway.exception.GatewayAuthenticationException.class
                )
        );
    }

    @Bean
    public CircuitBreakerConfigCustomizer authServiceCircuitBreaker() {
        return CircuitBreakerConfigCustomizer.of("auth-service", builder -> builder
                .failureRateThreshold(60)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .slidingWindowSize(5)
                .minimumNumberOfCalls(3)
        );
    }

    @Bean
    public CircuitBreakerConfigCustomizer flightServiceCircuitBreaker() {
        return CircuitBreakerConfigCustomizer.of("flight-service", builder -> builder
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .slidingWindowSize(8)
        );
    }

    @Bean
    public CircuitBreakerConfigCustomizer warehouseServiceCircuitBreaker() {
        return CircuitBreakerConfigCustomizer.of("warehouse-service", builder -> builder
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofSeconds(25))
                .slidingWindowSize(10)
        );
    }
}
