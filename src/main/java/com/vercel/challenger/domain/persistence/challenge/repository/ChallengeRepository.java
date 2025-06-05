package com.vercel.challenger.domain.persistence.challenge.repository;

import com.vercel.challenger.domain.persistence.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByUserIdAndCreatedAtIs(Long userId, LocalDateTime targetDate);
}
