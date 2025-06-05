package com.vercel.challenger.application.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Controller
public class MainController {
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
}
