package com.vercel.challenger.application.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChallengeRouter {
    @GetMapping("/challenge-management")
    public Mono<String> challengeManagement() {
        return Mono.just("challenge-management");
    }
}
