package com.vercel.challenger.domain.persistence.comment.entity;

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

@Entity
@Getter
@Table(name = "comment")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    private Comment(User writer, User receiver, String content) {
        Assert.notNull(writer, "writer must not be null");
        Assert.notNull(receiver, "receiver must not be null");
        Assert.hasText(content, "content must not be empty");

        this.writer = writer;
        this.receiver = receiver;
        this.content = content;
    }

    private Comment(User writer, User receiver, String content, Comment parentComment) {
        this(writer, receiver, content);

        Assert.notNull(parentComment, "parentComment must not be null");
        this.parentComment = parentComment;
    }

    public static Comment of(User writer, User receiver, String content) {
        return new Comment(writer, receiver, content);
    }

    public static Comment of(User writer, User receiver, String content, Comment parentComment) {
        return new Comment(writer, receiver, content, parentComment);
    }

    public void changeContent(String content) {
        Assert.hasText(content, "content must not be empty");
        this.content = content;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
