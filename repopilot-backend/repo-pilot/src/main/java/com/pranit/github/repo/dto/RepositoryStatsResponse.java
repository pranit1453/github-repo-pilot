package com.pranit.github.repo.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record RepositoryStatsResponse(
        long totalRepositories,
        long pendingIndexCount,
        long indexingCount,
        long completedIndexCount,
        long failedIndexCount,
        long totalFilesProcessed,
        long totalChunks,
        Map<String, Long> languages
) {
}
