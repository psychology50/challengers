package com.vercel.challenger.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    @Override
    public Mono<Void> onAuthenticationSuccess(@NonNull WebFilterExchange exchange, Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .flatMap(auth -> {
                    log.info("Successfully authenticated user: {}", auth.getName());
                    log.info("Authentication details: {}", auth.getDetails());
                    log.info("Authentication authorities: {}", auth.getAuthorities());
                    log.info("Authentication principal: {}", auth.getPrincipal());
                    log.info("Authentication credentials: {}", auth.getCredentials());

                    var redirectUri = UriComponentsBuilder
                            .fromUriString("/dashboard")
                            .build()
                            .toUri();

                    var handler = new RedirectServerAuthenticationSuccessHandler();
                    handler.setLocation(redirectUri);

                    return handler.onAuthenticationSuccess(exchange, auth);
                })
                .then();
    }
}
