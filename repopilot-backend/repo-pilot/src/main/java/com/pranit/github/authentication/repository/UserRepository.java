package com.pranit.github.authentication.repository;

import com.pranit.github.entities.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByGithubId(Long githubId);

    Optional<User> findByUserId(UUID userId);

    Optional<User> findByGithubUsername(String username);
}
