package com.lunazkoe.newsfeed.domain.user.repository;

import com.lunazkoe.newsfeed.domain.user.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);
}
