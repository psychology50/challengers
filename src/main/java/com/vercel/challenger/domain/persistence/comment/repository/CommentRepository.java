package com.vercel.challenger.domain.persistence.comment.repository;

import com.vercel.challenger.domain.persistence.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
