package com.pranit.github.repo.dto;


import com.pranit.github.entities.constant.IndexStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record IndexStatusResponse(
        UUID repositoryId,
        IndexStatus indexStatus,
        int filesTotal,
        int filesProcessed,
        int chunkCount,
        Instant indexedAt
) {
}
