package com.vercel.challenger.application.router;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Controller
public class AuthRouter {
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
}
