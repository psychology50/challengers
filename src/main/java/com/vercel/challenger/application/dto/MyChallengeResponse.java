package com.vercel.challenger.application.dto;

public record MyChallengeResponse(
        long id,
        String title,
        String description,
        int totalDays,
        int completedDays,
        int completionRate,
        boolean isTodayCompleted
) {
}
