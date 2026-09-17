package com.pranit.github.rag.pipeline.ingestion.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record RepositoryIndexingStartedEvent(
        UUID repositoryId,
        UUID userId
) {
}
