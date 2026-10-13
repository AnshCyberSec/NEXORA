package com.nexora.gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    private static final String LOG_PREFIX = "[GATEWAY-AUTH]";

    // Public routes — no JWT required
    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh"
    );

    // Header names for downstream services
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_ROLES = "X-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("{} Incoming request: {} {}", LOG_PREFIX, request.getMethod(), path);

        // 1. Skip public routes
        if (isPublicRoute(path)) {
            log.debug("{} Public route, skipping JWT: {}", LOG_PREFIX, path);
            return chain.filter(exchange);
        }

        // 2. Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("{} Missing/invalid Authorization header for: {}", LOG_PREFIX, path);
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }

        // 3. Extract JWT
        String token = authHeader.substring(7);

        // 4. Validate JWT
        Claims claims = jwtService.validateToken(token);
        if (claims == null) {
            log.warn("{} Invalid or expired JWT for: {}", LOG_PREFIX, path);
            return unauthorizedResponse(exchange, "Invalid or expired token");
        }

        // 5. Extract user info from claims
        String username = claims.getSubject();
        String userId = claims.get("userId", String.class);
        List<String> roles = claims.get("roles", List.class);

        log.info("{}  JWT valid — user: {}, userId: {}, roles: {}, path: {}",
                LOG_PREFIX, username, userId, roles, path);

        // 6. Inject headers for downstream services
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(HEADER_USER_ID, userId != null ? userId : "")
                .header(HEADER_USERNAME, username != null ? username : "")
                .header(HEADER_ROLES, roles != null ? String.join(",", roles) : "")
                .build();

        // 7. Forward request with modified headers
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicRoute(String path) {
        return PUBLIC_ROUTES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String body = String.format(
                "{\"success\":false,\"error\":\"UNAUTHORIZED\",\"message\":\"%s\",\"status\":401}",
                message
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Run early — before routing
        return -100;
    }
}