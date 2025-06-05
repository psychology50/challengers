package com.vercel.challenger.application.dto;

public record ParticipantResponse(
        long id,
        String name,
        String nameInitial,
        String commitmentMessage,
        int achievementRate,
        String profileImageUrl
) {
}
