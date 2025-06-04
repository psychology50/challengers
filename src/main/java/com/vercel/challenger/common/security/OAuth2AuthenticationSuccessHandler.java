package com.vercel.challenger.common.security;

import com.vercel.challenger.domain.persistence.user.entity.User;
import com.vercel.challenger.domain.persistence.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final Scheduler virtualThreadScheduler = Schedulers.fromExecutor(
            Executors.newVirtualThreadPerTaskExecutor()
    );

    @Override
    public Mono<Void> onAuthenticationSuccess(@NonNull WebFilterExchange exchange, Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .flatMap(auth -> {
                    log.info("Successfully authenticated user: {}", auth.getName());

                    if (auth.getPrincipal() instanceof OAuth2User oAuth2User) {
                        var oauthId = (String) oAuth2User.getAttribute("sub");

                        return checkAndSaveUser(oAuth2User, oauthId)
                                .then(performRedirect(exchange, auth));
                    }

                    return performRedirect(exchange, auth);
                })
                .then();
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    private Mono<Void> checkAndSaveUser(OAuth2User oAuth2User, String oauthId) {
        return Mono.fromCallable(() -> userRepository.existsByGoogleId(oauthId))
                .flatMap(exists -> {
                    if (exists) {
                        log.info("User with OAuth ID {} already exists", oauthId);
                        return Mono.empty();
                    } else {
                        log.info("Creating new user with OAuth ID {}", oauthId);

                        var name = (String) oAuth2User.getAttribute("name");
                        var email = (String) oAuth2User.getAttribute("email");
                        var picture = (String) oAuth2User.getAttribute("picture");
                        var user = User.of(oauthId, email, name, picture);

                        return saveUserAsync(user);
                    }
                })
                .subscribeOn(virtualThreadScheduler)
                .then();
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    private Mono<User> saveUserAsync(User user) {
        return Mono.fromCallable(() -> {
                    log.info("Saving new user: {}", user);
                    return userRepository.save(user);
                })
                .subscribeOn(virtualThreadScheduler)
                .doOnError(e -> log.error("Error saving user: {}", e.getMessage(), e))
                .onErrorMap(e -> new RuntimeException("Failed to save user", e));
    }

    private Mono<Void> performRedirect(WebFilterExchange exchange, Authentication auth) {
        var redirectUri = UriComponentsBuilder
                .fromUriString("/dashboard")
                .build()
                .toUri();

        var handler = new RedirectServerAuthenticationSuccessHandler();
        handler.setLocation(redirectUri);

        return handler.onAuthenticationSuccess(exchange, auth);
    }
}
