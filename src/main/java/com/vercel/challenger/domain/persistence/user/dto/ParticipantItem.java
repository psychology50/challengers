package com.vercel.challenger.domain.persistence.user.dto;

import com.vercel.challenger.domain.persistence.user.entity.User;

public record ParticipantItem(
        long id,
        String name,
        String nameInitial,
        String commitmentMessage,
        int achievementRate,
        String profileImageUrl
) {
    public static ParticipantItem of(User user, int achievementRate) {
        return new ParticipantItem(
                user.getId(),
                user.getName(),
                user.getName().substring(0, 1).toUpperCase(),
                user.getCommitmentMessage(),
                achievementRate,
                user.getProfileImageUrl()
        );
    }
}
