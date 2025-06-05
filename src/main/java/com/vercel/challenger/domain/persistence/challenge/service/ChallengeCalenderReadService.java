package com.vercel.challenger.domain.persistence.challenge.service;

import com.vercel.challenger.domain.persistence.challenge.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeCalenderReadService {
    private final ChallengeRepository challengeRepository;

    @Transactional(readOnly = true)
    public Map<String, Map<String, String>> execute(long userId, LocalDateTime targetDate) {
        var challenges = challengeRepository.findByUserIdAndCreatedAtIs(userId, targetDate);

        return challenges.stream()
                .flatMap(challenge -> challenge.getDailyProgresses().stream())
                .collect(
                        Collectors.groupingBy(
                                dailyProgress -> LocalDate.of(
                                        dailyProgress.getChallenge().getCreatedAt().getYear(),
                                        dailyProgress.getChallenge().getCreatedAt().getMonth(),
                                        dailyProgress.getTargetDay()
                                ).toString(),
                                Collectors.toMap(
                                        dailyProgress -> dailyProgress.getChallenge().getTitle(),
                                        dailyProgress -> dailyProgress.isCompleted() ? "completed" : (dailyProgress.getTargetDay() < targetDate.getDayOfMonth()) ? "missed" : "pending"
                                )
                        )
                );
    }
}
