package com.pranit.github.repo.service.impl;

import com.pranit.github.entities.constant.IndexStatus;
import com.pranit.github.repo.repository.RepositoryRepository;
import com.pranit.github.repo.service.RepositoryIndexProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RepositoryIndexProgressServiceImpl implements RepositoryIndexProgressService {

    private final RepositoryRepository repositoryRepository;

    @Override
    @Transactional
    public void updateProgress(
            final UUID repositoryId, final int totalFiles,
            final int processedFiles, final int indexedChunks) {
        repositoryRepository.updateIndexProgress(
                repositoryId, totalFiles,
                processedFiles, indexedChunks,
                IndexStatus.INDEXING, Instant.now());
    }

    @Override
    @Transactional
    public void updateStatus(final UUID repositoryId, final IndexStatus status) {
        repositoryRepository.updateIndexStatus(repositoryId, status);
    }
}
