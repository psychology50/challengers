package com.vercel.challenger.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.session.InMemoryReactiveSessionRegistry;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.SessionLimit;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

import java.net.URI;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ServerAuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ServerLogoutSuccessHandler customLogoutSuccessHandler,
            ServerSecurityContextRepository securityContextRepository
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .sessionManagement(session -> session
                        .concurrentSessions((spec) -> spec
                                .maximumSessions(SessionLimit.of(1))
                                .sessionRegistry(new InMemoryReactiveSessionRegistry())
                        )
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/", "/css/**", "/images/**", "/js/**").permitAll()
                        .pathMatchers("/", "/login", "/oauth2/**", "/login/oauth2/**", "/error", "/test/public").permitAll()
                        .pathMatchers("/admin/sessions").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .authenticationSuccessHandler(oauth2AuthenticationSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                )
                .securityContextRepository(securityContextRepository)
                .build();
    }

    @Bean
    public ServerLogoutSuccessHandler customLogoutSuccessHandler() {
        var handler = new RedirectServerLogoutSuccessHandler();
        handler.setLogoutSuccessUrl(URI.create("/"));
        return handler;
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        var repository = new WebSessionServerSecurityContextRepository();
        repository.setSpringSecurityContextAttrName("SPRING_SECURITY_CONTEXT");
        return repository;
    }
}
