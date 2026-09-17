package com.pranit.github.rag.pipeline.ingestion.dto;

import com.pranit.github.entities.constant.IndexStatus;
import com.pranit.github.entities.entity.Repository;
import lombok.Builder;

import java.util.UUID;

@Builder
public record IndexingResponse(
        UUID repositoryId,
        IndexStatus status,
        int filesTotal,
        int filesProcessed,
        int chunkCount
) {

    public static IndexingResponse from(Repository repository) {
        return IndexingResponse.builder()
                .repositoryId(repository.getRepositoryId())
                .status(repository.getIndexStatus())
                .filesTotal(repository.getFilesTotal())
                .filesProcessed(repository.getFilesProcessed())
                .chunkCount(repository.getChunkCount())
                .build();
    }
}