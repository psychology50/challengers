package com.vercel.challenger.application.dto;

import com.vercel.challenger.domain.persistence.user.entity.User;

public record UserResponse(
        long id,
        String name,
        String nameInitial,
        String email,
        String profileImageUrl
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getName().substring(0, 1).toUpperCase(),
                user.getEmail(),
                user.getProfileImageUrl()
        );
    }
}
