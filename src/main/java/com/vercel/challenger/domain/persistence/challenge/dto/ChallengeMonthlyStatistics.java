package com.vercel.challenger.domain.persistence.challenge.dto;

public record ChallengeMonthlyStatistics(
        int achievementRate,
        int totalChallenges,
        int completedDays,
        int targetDays,
        int consecutiveDays
) {
    public static ChallengeMonthlyStatistics of(
            int achievementRate,
            int totalChallenges,
            int completedDays,
            int targetDays,
            int consecutiveDays
    ) {
        return new ChallengeMonthlyStatistics(
                achievementRate,
                totalChallenges,
                completedDays,
                targetDays,
                consecutiveDays
        );
    }

    public static ChallengeMonthlyStatistics empty() {
        return new ChallengeMonthlyStatistics(0, 0, 0, 0, 0);
    }
}
