package com.vercel.challenger.domain.persistence.user.repository;

import com.vercel.challenger.domain.persistence.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByGoogleId(String googleId);

    Optional<User> findByGoogleId(String googleId);
}
