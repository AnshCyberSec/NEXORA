package com.nexora.payment.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {


     // Circuit Breaker Configuration for Wallet Service calls

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // Kitne calls fail hone ke baad circuit OPEN ho
                .failureRateThreshold(50)  // 50% failures

                // Sliding window - kitne calls track kare
                .slidingWindowSize(10)  // Last 10 calls

                // Minimum calls - itne calls ke baad hi evaluate karo
                .minimumNumberOfCalls(5)

                // OPEN state mein kitni der rahe
                .waitDurationInOpenState(Duration.ofSeconds(30))

                // HALF-OPEN mein kitne test calls allow kare
                .permittedNumberOfCallsInHalfOpenState(3)

                // Kaunse exceptions ko failure mana jaye
                .recordExceptions(
                        java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class,
                        feign.FeignException.class,
                        feign.RetryableException.class
                )
                .build();

        return CircuitBreakerRegistry.of(config);
    }


    // Retry Configuration for Wallet Service calls

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                // Kitni baar retry kare
                .maxAttempts(3)

                // Har retry ke beech kitna wait
                .waitDuration(Duration.ofMillis(500))

                // Kaunse exceptions pe retry kare
                .retryExceptions(
                        java.io.IOException.class,
                        feign.RetryableException.class,
                        feign.FeignException.ServiceUnavailable.class,
                        feign.FeignException.GatewayTimeout.class
                )
                .build();

        return RetryRegistry.of(config);
    }


     //Time Limiter Configuration for Wallet Service calls

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                // Maximum kitni der wait kare
                .timeoutDuration(Duration.ofSeconds(3))
                .build();

        return TimeLimiterRegistry.of(config);
    }
}