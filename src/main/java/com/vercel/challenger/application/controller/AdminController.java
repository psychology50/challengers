package com.vercel.challenger.application.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.session.MapSession;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/sessions")
@RequiredArgsConstructor
public class AdminController {
    private final ReactiveSessionRepository<MapSession> sessionRepository;

    @GetMapping
    public Mono<String> listSessions(Model model) {
        return getAllSessions()
                .collectList()
                .doOnNext(sessions -> {
                    log.info("Found {} sessions", sessions.size());
                    model.addAttribute("sessions", sessions);
                    model.addAttribute("totalSessions", sessions.size());
                    model.addAttribute("activeSessions", sessions.stream()
                            .filter(dto -> !dto.isExpired())
                            .count());
                    model.addAttribute("authenticatedSessions", sessions.stream()
                            .filter(SessionDto::isAuthenticated)
                            .count());
                })
                .then(Mono.just("admin/sessions"));
    }

    private Flux<SessionDto> getAllSessions() {
        if (sessionRepository instanceof ReactiveMapSessionRepository mapRepo) {
            return getSessionsFromMapRepository(mapRepo);
        }

        return Flux.empty();
    }

    @SuppressWarnings("unchecked")
    private Flux<SessionDto> getSessionsFromMapRepository(ReactiveMapSessionRepository mapRepo) {
        try {
            Field sessionsField = ReactiveMapSessionRepository.class.getDeclaredField("sessions");
            sessionsField.setAccessible(true);
            Map<String, MapSession> sessions = (Map<String, MapSession>) sessionsField.get(mapRepo);

            return Flux.fromIterable(sessions.values())
                    .map(this::convertToDto);
        } catch (Exception e) {
            log.error("Error accessing sessions map", e);
            return Flux.empty();
        }
    }

    private SessionDto convertToDto(MapSession session) {
        SessionDto dto = new SessionDto();
        dto.setSessionId(session.getId());
        dto.setCreationTime(session.getCreationTime());
        dto.setLastAccessTime(session.getLastAccessedTime());
        dto.setMaxInactiveInterval(session.getMaxInactiveInterval());
        dto.setExpired(session.isExpired());

        Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContext instanceof SecurityContext context) {
            Authentication auth = context.getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                dto.setAuthenticated(true);
                dto.setUsername(auth.getName());

                if (auth instanceof OAuth2AuthenticationToken oauth2Token) {
                    dto.setOauth2Provider(oauth2Token.getAuthorizedClientRegistrationId());
                }
            }
        }

        return dto;
    }

    @Setter
    @Getter
    public static class SessionDto {
        private String sessionId;
        private Instant creationTime;
        private Instant lastAccessTime;
        private Duration maxInactiveInterval;
        private boolean expired;
        private boolean authenticated = false;
        private String username;
        private String oauth2Provider;

        public String getShortSessionId() {
            return sessionId != null && sessionId.length() > 8 ?
                    sessionId.substring(0, 8) + "..." : sessionId;
        }

        public String getStatus() {
            if (expired) return "만료됨";
            if (authenticated) return "인증됨";
            return "익명";
        }
    }
}