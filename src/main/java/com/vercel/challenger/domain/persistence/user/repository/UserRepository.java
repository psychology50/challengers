package com.vercel.challenger.domain.persistence.user.repository;

import com.vercel.challenger.domain.persistence.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByGoogleId(String googleId);

}
