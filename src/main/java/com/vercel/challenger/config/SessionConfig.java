package com.vercel.challenger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSession;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableSpringWebSession
public class SessionConfig {
    @Bean
    public ReactiveSessionRepository<MapSession> reactiveSessionRepository() {
        var reactiveMapSessionRepository = new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
        reactiveMapSessionRepository.setDefaultMaxInactiveInterval(Duration.ofDays(7));

        return reactiveMapSessionRepository;
    }

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        var resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("SESSION");
        resolver.addCookieInitializer(builder -> builder
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
        );

        return resolver;
    }
}
