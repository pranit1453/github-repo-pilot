package com.pranit.github.rag.ingestion.service;

import com.pranit.github.rag.ingestion.dto.IndexingResponse;

import java.util.UUID;

@FunctionalInterface
public interface RepositoryIndexingService {

    IndexingResponse startIndexing(UUID repoId, UUID userId);
}
