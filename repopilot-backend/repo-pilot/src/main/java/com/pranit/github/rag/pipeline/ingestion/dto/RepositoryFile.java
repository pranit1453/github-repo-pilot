package com.pranit.github.rag.pipeline.ingestion.dto;

import lombok.Builder;

@Builder
public record RepositoryFile(
        String path,
        String content
) {
}
