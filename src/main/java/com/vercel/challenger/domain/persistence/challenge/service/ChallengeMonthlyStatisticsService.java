package com.vercel.challenger.domain.persistence.challenge.service;

import com.vercel.challenger.domain.persistence.challenge.dto.ChallengeMonthlyStatistics;
import com.vercel.challenger.domain.persistence.challenge.entity.Challenge;
import com.vercel.challenger.domain.persistence.challenge.repository.ChallengeRepository;
import com.vercel.challenger.domain.persistence.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChallengeMonthlyStatisticsService {
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    @Transactional(readOnly = true)
    public ChallengeMonthlyStatistics execute(long userId, LocalDateTime targetDate) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        var challenges = challengeRepository.findByUserIdAndCreatedAtIs(userId, targetDate);

        if (challenges.isEmpty()) {
            return ChallengeMonthlyStatistics.empty();
        }

        var totalCompletionRate = (int) challenges.stream()
                .mapToInt(Challenge::getCompletionRate)
                .average()
                .orElse(0.0);

        var targetDays = (int) challenges.stream()
                .map(Challenge::getDailyProgresses)
                .count();

        return ChallengeMonthlyStatistics.of(totalCompletionRate, challenges.size(), user.getCompletedDays(), targetDays, user.getConsecutiveDays());
    }
}
