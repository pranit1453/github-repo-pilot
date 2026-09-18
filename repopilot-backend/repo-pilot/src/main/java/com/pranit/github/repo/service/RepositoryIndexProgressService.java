package com.pranit.github.repo.service;

import com.pranit.github.entities.constant.IndexStatus;

import java.util.UUID;

public interface RepositoryIndexProgressService {

    void updateProgress(UUID repositoryId, int totalFiles, int processedFiles, int indexedChunks);

    void updateStatus(UUID repositoryId, IndexStatus status);
}
