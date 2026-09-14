package com.pranit.github.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pranit.github.entities.constant.IndexStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RepositoryResponse(
        UUID id,
        Long githubRepoId,
        String owner,
        String name,
        String fullName,
        @JsonProperty("isPrivate") boolean isPrivate,
        String defaultBranch,
        String language,
        String htmlUrl,
        String description,
        IndexStatus indexStatus,
        Instant indexedAt,
        int chunkCount,
        int filesTotal,
        int filesProcessed
) {
}
