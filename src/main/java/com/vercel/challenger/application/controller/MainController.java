package com.vercel.challenger.application.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Controller
public class MainController {
    @GetMapping("/")
    public Mono<String> home(ServerWebExchange exchange) {
        var sessionCookie = exchange.getRequest().getCookies().getFirst("SESSION");

        if (sessionCookie == null) {
            log.info("No session cookie - new user");
            return Mono.just("index");
        }

        log.info("Session cookie found: {}", sessionCookie.getValue());

        return exchange.getSession()
                .flatMap(session -> {
                    log.info("Session ID: {}", session.getId());

                    var securityContextObj = session.getAttributes().get("SPRING_SECURITY_CONTEXT");

                    if (securityContextObj instanceof SecurityContext securityContext) {
                        var auth = securityContext.getAuthentication();

                        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                            log.info("Valid authentication found in session: {}", auth.getName());
                            return Mono.just("redirect:/dashboard");
                        }
                    }

                    log.info("No valid authentication in session");
                    return Mono.just("index");
                })
                .onErrorResume(error -> {
                    log.error("Error checking session: ", error);
                    return Mono.just("index");
                });
    }

    @GetMapping("/login")
    public Mono<Void> login(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create("/oauth2/authorization/google"));
        return response.setComplete();
    }

    @GetMapping("/dashboard")
    public Mono<String> dashboard(
            @AuthenticationPrincipal OAuth2User principal,
            WebSession session,
            Model model
    ) {
        return Mono.fromCallable(() -> {
            // 사용자 정보를 세션에 저장
            session.getAttributes().put("user_name", principal.getAttribute("name"));
            session.getAttributes().put("user_email", principal.getAttribute("email"));
            session.getAttributes().put("user_picture", principal.getAttribute("picture"));
            session.getAttributes().put("login_time", System.currentTimeMillis());

            // 모델에 데이터 추가
            model.addAttribute("name", principal.getAttribute("name"));
            model.addAttribute("email", principal.getAttribute("email"));
            model.addAttribute("picture", principal.getAttribute("picture"));
            model.addAttribute("sessionId", session.getId());
            model.addAttribute("loginTime", LocalDateTime.now().toString());

            return "dashboard";
        });
    }

    @GetMapping("/session-info")
    public Mono<String> sessionInfo(WebSession session, Model model) {
        return Mono.fromCallable(() -> {
            // 세션에서 정보 가져오기
            String userName = session.getAttribute("user_name");
            String userEmail = session.getAttribute("user_email");
            String userPicture = session.getAttribute("user_picture");
            Long loginTime = session.getAttribute("login_time");

            model.addAttribute("sessionId", session.getId());
            model.addAttribute("userName", userName);
            model.addAttribute("userEmail", userEmail);
            model.addAttribute("userPicture", userPicture);

            if (loginTime != null) {
                LocalDateTime loginDateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(loginTime),
                        ZoneId.systemDefault()
                );
                model.addAttribute("loginTime", loginDateTime.toString());
            }

            model.addAttribute("sessionTimeout", session.getMaxIdleTime().getSeconds());
            model.addAttribute("creationTime", session.getCreationTime().toString());
            model.addAttribute("lastAccessTime", session.getLastAccessTime().toString());

            return "session-info";
        });
    }
}
