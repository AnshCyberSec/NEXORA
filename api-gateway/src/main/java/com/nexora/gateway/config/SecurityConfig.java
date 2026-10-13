package com.nexora.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF (stateless API with JWT)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Disable default form login
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // Disable HTTP basic auth
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // Allow ALL requests — JWT validation happens in JwtAuthenticationFilter
                .authorizeExchange(exchange -> exchange
                        .anyExchange().permitAll()
                )
                .build();
    }
}