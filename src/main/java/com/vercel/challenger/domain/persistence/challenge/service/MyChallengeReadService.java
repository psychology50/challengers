package com.vercel.challenger.domain.persistence.challenge.service;

import com.vercel.challenger.domain.persistence.challenge.repository.ChallengeRepository;
import com.vercel.challenger.domain.persistence.daily_progress.dto.ChallengeItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyChallengeReadService {
    private final ChallengeRepository challengeRepository;

    @Transactional(readOnly = true)
    public List<ChallengeItem> execute(long userId, LocalDateTime targetDate) {
        var challenges = challengeRepository.findByUserIdAndCreatedAtIs(userId, targetDate);

        return challenges.stream()
                .map(challenge -> ChallengeItem.of(challenge, targetDate))
                .toList();
    }
}
