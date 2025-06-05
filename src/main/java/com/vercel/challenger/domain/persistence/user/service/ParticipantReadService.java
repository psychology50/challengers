package com.vercel.challenger.domain.persistence.user.service;

import com.vercel.challenger.domain.persistence.challenge.entity.Challenge;
import com.vercel.challenger.domain.persistence.challenge.repository.ChallengeRepository;
import com.vercel.challenger.domain.persistence.user.dto.ParticipantItem;
import com.vercel.challenger.domain.persistence.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantReadService {
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    @Transactional(readOnly = true)
    public List<ParticipantItem> execute(long excludeUserId, LocalDateTime targetDate) {
        var users = userRepository.findAll();

        return users.stream()
                .filter(user -> user.getId() != excludeUserId)
                .map(user -> {
                    var challenge = challengeRepository.findByUserIdAndCreatedAtIs(user.getId(), targetDate);
                    var totalCompletionRate = (int) challenge.stream()
                            .mapToInt(Challenge::getCompletionRate)
                            .average()
                            .orElse(0.0);

                    return ParticipantItem.of(user, totalCompletionRate);
                })
                .toList();
    }
}
