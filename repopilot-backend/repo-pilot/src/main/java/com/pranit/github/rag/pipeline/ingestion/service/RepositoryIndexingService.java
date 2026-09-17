package com.pranit.github.rag.pipeline.ingestion.service;

import com.pranit.github.rag.pipeline.ingestion.dto.IndexingResponse;

import java.util.UUID;

@FunctionalInterface
public interface RepositoryIndexingService {

    IndexingResponse startIndexing(UUID repoId, UUID userId);
}
