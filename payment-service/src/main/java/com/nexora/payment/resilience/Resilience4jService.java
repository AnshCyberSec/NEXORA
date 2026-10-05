package com.nexora.payment.resilience;

import com.nexora.payment.client.WalletClient;
import com.nexora.payment.dto.request.WalletDebitRequest;
//import com.nexora.payment.dto.request.WalletDebitRequest;
import com.nexora.payment.dto.response.WalletDebitResponse;
//import com.nexora.payment.dto.response.WalletDebitResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class Resilience4jService {

    private final WalletClient walletClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    private static final String LOG_PREFIX = "[RESILIENCE]";
    private static final String WALLET_SERVICE = "walletService";
    private static final long TIMEOUT_SECONDS = 3;

    /**
     * Debit wallet with resilience: Circuit Breaker + Retry + Timeout + Fallback
     */
    public WalletDebitResponse debitWalletWithResilience(String walletId, WalletDebitRequest request) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(WALLET_SERVICE);
        Retry retry = retryRegistry.retry(WALLET_SERVICE);

        Supplier<WalletDebitResponse> walletCall = () -> {
            log.debug("{} Calling Wallet Service. walletId: {}, amount: {}",
                    LOG_PREFIX, walletId, request.getAmount());
            return walletClient.debitWallet(walletId, request);
        };

        // Apply Circuit Breaker + Retry
        Supplier<WalletDebitResponse> decoratedSupplier = CircuitBreaker
                .decorateSupplier(circuitBreaker, Retry.decorateSupplier(retry, walletCall));

        // Execute with manual timeout using ExecutorService
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<WalletDebitResponse> future = executor.submit(decoratedSupplier::get);
            WalletDebitResponse response = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info("{} Wallet Service call successful. CircuitBreaker state: {}",
                    LOG_PREFIX, circuitBreaker.getState());

            return response;

        } catch (TimeoutException e) {
            log.error("{} Wallet Service call TIMEOUT after {}s. CircuitBreaker state: {}",
                    LOG_PREFIX, TIMEOUT_SECONDS, circuitBreaker.getState());
            return buildFallbackResponse(walletId, request, e);

        } catch (Exception e) {
            log.error("{} Wallet Service call FAILED. CircuitBreaker state: {}, Error: {}",
                    LOG_PREFIX, circuitBreaker.getState(), e.getMessage());
            return buildFallbackResponse(walletId, request, e);

        } finally {
            executor.shutdownNow();
        }
    }

    private WalletDebitResponse buildFallbackResponse(String walletId, WalletDebitRequest request, Exception e) {
        log.warn("{} FALLBACK triggered for walletId: {}, reason: {}",
                LOG_PREFIX, walletId, e.getClass().getSimpleName());

        return WalletDebitResponse.builder()
                .success(false)
                .walletId(walletId)
                .amount(request.getAmount())
                .message("Wallet Service is temporarily unavailable. Please try again later.")
                .build();
    }

    public String getCircuitBreakerState() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(WALLET_SERVICE);
        return circuitBreaker.getState().name();
    }
}