package com.vercel.challenger.domain.persistence.daily_progress.repository;

import com.vercel.challenger.domain.persistence.daily_progress.entity.DailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyProgressRepository extends JpaRepository<DailyProgress, Long> {
}
