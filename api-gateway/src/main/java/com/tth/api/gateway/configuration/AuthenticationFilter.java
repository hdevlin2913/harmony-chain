package com.tth.api.gateway.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tth.api.gateway.dto.APIResponse;
import com.tth.api.gateway.service.IdentityService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @NonFinal
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/cart/.*",

            "/identity/auth/.*",
            "/identity/users/.*",

            "/inventory/.*",

            "/order/.*",

            "/product/.*",

            "/profile/.*",

            "/rating/.*",

            "/shipping/.*"
    );

    private final ObjectMapper objectMapper;
    private final IdentityService identityService;

    @Value("${app.api-prefix}")
    private String apiPrefix;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (this.isPublicEndpoint(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        List<String> authToken = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authToken)) {
            return this.unauthenticated(exchange.getResponse());
        }

        String token = authToken.getFirst().substring("Bearer ".length());
        return this.identityService.introspect(token).flatMap(response -> {
            if (response.getResult().isValid()) {
                return chain.filter(exchange);
            }

            return this.unauthenticated(exchange.getResponse());
        }).onErrorResume(throwable -> this.unauthenticated(exchange.getResponse()));
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

    private Mono<Void> unauthenticated(ServerHttpResponse response) {
        APIResponse<?> apiResponse = APIResponse.builder().code(1401).message("Unauthenticated").build();

        String body;
        try {
            body = this.objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

}