package com.parkease.apigateway.filter;

import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements GlobalFilter {

    @org.springframework.beans.factory.annotation.Value("${jwt.secret:P@rkEase_Super_S3cur3_JWT_K3y_2026_!@#$}")
    private String secret;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        logger.info("PATH: {}", path);

        // allow login + register
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // allow public GET requests to lots and spots for Landing Page
        if (exchange.getRequest().getMethod().name().equals("GET") && (path.startsWith("/lots") || path.startsWith("/spots"))) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes())
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}