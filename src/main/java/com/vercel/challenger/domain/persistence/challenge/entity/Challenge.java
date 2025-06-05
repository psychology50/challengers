package com.vercel.challenger.domain.persistence.challenge.entity;

import com.vercel.challenger.domain.persistence.daily_progress.entity.DailyProgress;
import com.vercel.challenger.domain.persistence.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "challenge")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge {
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<DailyProgress> dailyProgresses = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description", nullable = true)
    private String description;
    @Column(name = "target_month", nullable = true)
    private YearMonth targetMonth;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Challenge(User user, String title, String description, YearMonth targetMonth) {
        Assert.notNull(user, "user must not be null");
        Assert.hasText(title, "title must not be empty");
        Assert.notNull(targetMonth, "targetMonth must not be null");

        connectUser(user);
        this.title = title;
        this.description = description;
        this.targetMonth = targetMonth;
    }

    public static Challenge of(User user, String title, String description, YearMonth targetMonth) {
        return new Challenge(user, title, description, targetMonth);
    }

    public void update(String title, String description) {
        Assert.hasText(title, "title must not be empty");

        this.title = title;
        this.description = description;
    }

    public boolean isCompletedOn(LocalDateTime date) {
        Assert.notNull(date, "date must not be null");

        return dailyProgresses.stream()
                .anyMatch(dp -> dp.getTargetDay() == date.getDayOfMonth() && dp.isCompleted());
    }

    public int getTotalDays() {
        if (dailyProgresses.isEmpty()) {
            return 0;
        }

        return dailyProgresses.size();
    }

    public int getCompletedDays() {
        if (dailyProgresses.isEmpty()) {
            return 0;
        }

        return (int) dailyProgresses.stream()
                .filter(DailyProgress::isCompleted)
                .count();
    }

    public int getCompletionRate() {
        if (dailyProgresses.isEmpty()) {
            return 0;
        }

        var completedDays = dailyProgresses.stream()
                .filter(DailyProgress::isCompleted)
                .count();

        return (int) ((completedDays * 100) / dailyProgresses.size());
    }

    private void connectUser(User user) {
        if (this.user != null) {
            this.user.getChallenges().remove(this);
        }

        this.user = user;
        this.user.getChallenges().add(this);
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", targetMonth=" + targetMonth +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
