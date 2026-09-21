package com.pranit.github.rag.ingestion.service.impl;

import com.pranit.github.entities.constant.IndexStatus;
import com.pranit.github.entities.entity.Repository;
import com.pranit.github.rag.ingestion.dto.IndexingResponse;
import com.pranit.github.rag.ingestion.dto.RepositoryIndexingStartedEvent;
import com.pranit.github.rag.ingestion.service.RepositoryIndexingService;
import com.pranit.github.repo.exception.RepositoryAlreadyExistsException;
import com.pranit.github.repo.exception.RepositoryNotFoundException;
import com.pranit.github.repo.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryIndexingServiceImpl implements RepositoryIndexingService {

    private final RepositoryRepository repositoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public IndexingResponse startIndexing(final UUID repositoryId, final UUID userId) {
        Repository repository = repositoryRepository.findByRepositoryIdAndUserId(repositoryId, userId)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found"));
        validateIndexing(repository);
        repository = initializeIndexing(repository);
        eventPublisher.publishEvent(RepositoryIndexingStartedEvent.builder()
                .repositoryId(repositoryId)
                .userId(userId)
                .build());
        return IndexingResponse.from(repository);
    }

    private void validateIndexing(final Repository repository) {
        if (repository.getIndexStatus() == IndexStatus.INDEXING) {
            throw new RepositoryAlreadyExistsException("Repository is already being indexed");
        }
    }

    private Repository initializeIndexing(final Repository repository) {
        repository.setIndexStatus(IndexStatus.INDEXING);
        repository.setFilesProcessed(0);
        repository.setFilesTotal(0);
        repository.setChunkCount(0);
        repository.setUpdatedAt(Instant.now());
        return repositoryRepository.save(repository);
    }
}
