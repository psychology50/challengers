package com.vercel.challenger.domain.persistence.challenge.repository;

import com.vercel.challenger.domain.persistence.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}
