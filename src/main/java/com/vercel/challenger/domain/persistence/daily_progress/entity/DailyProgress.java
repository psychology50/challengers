package com.vercel.challenger.domain.persistence.daily_progress.entity;

import com.vercel.challenger.domain.persistence.challenge.entity.Challenge;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "daily_progress")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_day", nullable = false)
    private int targetDay;

    @Column(name = " completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    private DailyProgress(Challenge challenge, int targetDay) {
        Assert.notNull(challenge, "challenge must not be null");

        connectChallenge(challenge);
        this.targetDay = targetDay;
    }

    public static DailyProgress of(Challenge challenge, int targetDay) {
        return new DailyProgress(challenge, targetDay);
    }

    public void complete() {
        Assert.notNull(completedAt, "completedAt must not be null");
        this.completedAt = LocalDateTime.now();
    }

    private void connectChallenge(Challenge challenge) {
        if (this.challenge != null) {
            this.challenge.getDailyProgresses().remove(this);
        }
        this.challenge = challenge;
        challenge.getDailyProgresses().add(this);
    }

    @Override
    public String toString() {
        return "DailyProgress{" +
                "id=" + id +
                ", targetDay=" + targetDay +
                ", completedAt=" + completedAt +
                ", challenge=" + challenge.getId() +
                '}';
    }
}
