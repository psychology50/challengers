package com.vercel.challenger.domain.persistence.daily_progress.dto;

import com.vercel.challenger.domain.persistence.challenge.entity.Challenge;

import java.time.LocalDateTime;

public record ChallengeItem(
        long id,
        String title,
        String description,
        String scheduleText,
        int completedDays,
        int totalDays,
        int completionRate,
        boolean isTodayCompleted
) {
    public static ChallengeItem of(Challenge challenge, LocalDateTime now) {
        return new ChallengeItem(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                "", // 임시로 처리.
                challenge.getCompletedDays(),
                challenge.getTotalDays(),
                challenge.getCompletionRate(),
                challenge.isCompletedOn(now)
        );
    }
}
