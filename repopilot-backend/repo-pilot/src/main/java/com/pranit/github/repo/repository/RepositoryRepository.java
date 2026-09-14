package com.pranit.github.repo.repository;

import com.pranit.github.entities.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface RepositoryRepository extends JpaRepository<Repository, UUID>, JpaSpecificationExecutor<Repository> {

    List<Repository> findByUserId(UUID userId);

    List<Repository> findByUserIdOrderByFullNameAsc(UUID userId);

    Optional<Repository> findByRepositoryIdAndUserId(UUID repositoryId, UUID userId);

    Optional<Repository> findByUserIdAndGithubRepoId(UUID userId, Long githubRepoId);

    Optional<Repository> findByRepositoryId(UUID repoId);
}