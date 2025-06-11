package com.vercel.challenger.application.router;

import com.vercel.challenger.application.dto.CurrentMonthResponse;
import com.vercel.challenger.application.dto.UserResponse;
import com.vercel.challenger.domain.persistence.challenge.service.ChallengeCalenderReadService;
import com.vercel.challenger.domain.persistence.challenge.service.ChallengeMonthlyStatisticsService;
import com.vercel.challenger.domain.persistence.challenge.service.MyChallengeReadService;
import com.vercel.challenger.domain.persistence.user.service.ParticipantReadService;
import com.vercel.challenger.domain.persistence.user.service.UserReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardRouter {
    private final UserReadService userSearchService;
    private final MyChallengeReadService myChallengesReadService;
    private final ChallengeCalenderReadService challengeCalendarReadService;
    private final ChallengeMonthlyStatisticsService challengeMonthlyStatisticsService;
    private final ParticipantReadService participantReadService;

    @GetMapping("/dashboard")
    public Mono<String> dashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        log.info("principal = {}", principal);
        if (principal != null) {
            log.info("User ID: {}", (String) principal.getAttribute("sub"));
        }

        return Mono.fromCallable(() -> userSearchService.execute(principal.getName()))
                .flatMap(user -> {
                    model.addAttribute("user", UserResponse.from(user));

                    var now = LocalDateTime.now();
                    model.addAttribute("currentMonth", CurrentMonthResponse.from(now));

                    var challenges = Mono.fromCallable(() -> myChallengesReadService.execute(user.getId(), now))
                            .doOnNext(data -> model.addAttribute("myChallenges", data))
                            .then();

                    var calendar = Mono.fromCallable(() -> challengeCalendarReadService.execute(user.getId(), now))
                            .doOnNext(data -> model.addAttribute("challengeCalendarData", data))
                            .then();

                    var statistics = Mono.fromCallable(() -> challengeMonthlyStatisticsService.execute(user.getId(), now))
                            .doOnNext(data -> model.addAttribute("statistics", data))
                            .then();

                    var participants = Mono.fromCallable(() -> participantReadService.execute(user.getId(), now))
                            .doOnNext(data -> model.addAttribute("participants", data))
                            .then();

                    return Mono.when(challenges, calendar, statistics, participants)
                            .thenReturn("dashboard");
                })
                .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()));
    }
}
