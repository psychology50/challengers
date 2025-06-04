package com.vercel.challenger.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Base64;

@Slf4j
@Component
public class CookieOAuth2AuthorizationRequestRepository implements ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public Mono<OAuth2AuthorizationRequest> loadAuthorizationRequest(ServerWebExchange exchange) {
        String cookieValue = getCookie(exchange, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);

        if (cookieValue == null || cookieValue.trim().isEmpty()) {
            return Mono.empty();  // 🔥 쿠키가 없으면 빈 Mono 반환
        }

        try {
            OAuth2AuthorizationRequest request = deserialize(cookieValue);
            return Mono.justOrEmpty(request);  // 🔥 null-safe
        } catch (Exception e) {
            log.warn("Error deserializing authorization request: {}", e.getMessage());
            return Mono.empty();
        }
    }

    @Override
    public Mono<Void> saveAuthorizationRequest(OAuth2AuthorizationRequest request, ServerWebExchange exchange) {
        return Mono.fromRunnable(() -> {
            if (request == null) {
                deleteCookie(exchange, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
                return;
            }
            addCookie(exchange, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                    serialize(request), COOKIE_EXPIRE_SECONDS);
        });
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> removeAuthorizationRequest(ServerWebExchange exchange) {
        return loadAuthorizationRequest(exchange)
                .doOnNext(request -> deleteCookie(exchange, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME));
    }

    private String getCookie(ServerWebExchange exchange, String name) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(name);
        return cookie != null ? cookie.getValue() : null;
    }

    private void addCookie(ServerWebExchange exchange, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .maxAge(Duration.ofSeconds(maxAge))
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .build();

        exchange.getResponse().addCookie(cookie);
    }

    private void deleteCookie(ServerWebExchange exchange, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .path("/")
                .build();

        exchange.getResponse().addCookie(cookie);
    }

    private String serialize(OAuth2AuthorizationRequest request) {
        // 간단한 JSON 직렬화 (실제로는 더 복잡한 구현 필요)
        return Base64.getUrlEncoder().encodeToString(request.getState().getBytes());
    }

    private OAuth2AuthorizationRequest deserialize(String cookie) {
        String state = new String(Base64.getUrlDecoder().decode(cookie));
        return OAuth2AuthorizationRequest.authorizationCode()
                .clientId("your-client-id")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .state(state)
                .build();
    }
}