package com.pranit.github.repo.repository;

import com.pranit.github.entities.constant.IndexStatus;
import com.pranit.github.entities.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryRepository extends JpaRepository<Repository, UUID>, JpaSpecificationExecutor<Repository> {

    List<Repository> findByUserId(UUID userId);

    @Modifying
    @Query("""
                update Repository r
                   set r.filesTotal = :totalFiles,
                       r.filesProcessed = :processedFiles,
                       r.chunkCount = :indexedChunks,
                       r.indexStatus = :status,
                       r.updatedAt = :updatedAt
                 where r.repositoryId = :repositoryId
            """)
    void updateIndexProgress(
            @Param("repositoryId") UUID repositoryId,
            @Param("totalFiles") int totalFiles,
            @Param("processedFiles") int processedFiles,
            @Param("indexedChunks") int indexedChunks,
            @Param("status") IndexStatus status,
            @Param("updatedAt") Instant updatedAt
    );

    @Modifying
    @Query("""
                update Repository r
                   set r.indexStatus = :status,
                       r.updatedAt = CURRENT_TIMESTAMP
                 where r.repositoryId = :repositoryId
            """)
    void updateIndexStatus(@Param("repositoryId") UUID repositoryId, @Param("status") IndexStatus status);

    Optional<Repository> findByRepositoryIdAndUserId(UUID repositoryId, UUID userId);

    Optional<Repository> findByUserIdAndGithubRepoId(UUID userId, Long githubRepoId);

    Optional<Repository> findByRepositoryId(UUID repoId);
}