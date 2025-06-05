package com.vercel.challenger.domain.persistence.user.service;

import com.vercel.challenger.domain.persistence.user.entity.User;
import com.vercel.challenger.domain.persistence.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserReadService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute(String oauthId) {
        return userRepository.findByGoogleId(oauthId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with oauthId: " + oauthId));
    }
}
