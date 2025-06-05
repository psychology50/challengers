package com.vercel.challenger.application.dto;

public record StatisticsResponse(
        int achievementRate,
        int totalChallenges,
        int completedDays,
        int targetDays,
        int consecutiveDays
) {
}
