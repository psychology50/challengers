package com.vercel.challenger.domain.persistence.user.entity;

import com.vercel.challenger.domain.persistence.challenge.entity.Challenge;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Challenge> challenges = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "google_id", nullable = false, unique = true)
    private String googleId;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    @Column(name = "commitment_message")
    private String commitmentMessage;
    @Column(name = "completed_days")
    private int completedDays;
    @Column(name = "consecutive_days")
    private int consecutiveDays;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private User(String googleId, String email, String name, String profileImageUrl) {
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.commitmentMessage = "";
    }

    public static User of(String googleId, String email, String name, String profileImageUrl) {
        return new User(googleId, email, name, profileImageUrl);
    }

    public void increaseCompletedDays() {
        this.completedDays++;
    }

    public void increaseConsecutiveDays() {
        this.consecutiveDays++;
    }

    public void resetConsecutiveDays() {
        this.consecutiveDays = 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", googleId='" + googleId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", commitmentMessage='" + commitmentMessage + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
