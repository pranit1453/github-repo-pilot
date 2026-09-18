package com.pranit.github.rag.orchestrator;

import com.pranit.github.entities.constant.IndexStatus;
import com.pranit.github.entities.entity.Repository;
import com.pranit.github.rag.ingestion.dto.RepositoryIndexingStartedEvent;
import com.pranit.github.rag.ingestion.service.RepositoryIndexingSyncEventService;
import com.pranit.github.repo.exception.RepositoryNotFoundException;
import com.pranit.github.repo.repository.RepositoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
public abstract class IngestionOrchestrator {

    private final RepositoryRepository repositoryRepository;
    private final RepositoryIndexingSyncEventService syncEventService;

    protected IngestionOrchestrator(
            RepositoryRepository repositoryRepository,
            RepositoryIndexingSyncEventService syncEventService) {
        this.repositoryRepository = repositoryRepository;
        this.syncEventService = syncEventService;
    }

    @Async("indexingExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(final RepositoryIndexingStartedEvent event) {
        execute(event.repositoryId(), event.userId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(final UUID repositoryId, final UUID userId) {
        final Repository repository = repositoryRepository.findByRepositoryIdAndUserId(repositoryId, userId)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found"));
        try {
            ingestRepository(repository, userId);
            repositoryRepository.updateIndexStatus(repositoryId, IndexStatus.INDEXED);
            syncEventService.notify(repository.getRepositoryId(), IndexStatus.INDEXED);
        } catch (Exception e) {
            log.error("Repository indexing failed. repositoryId: {}", repositoryId, e);
            repositoryRepository.updateIndexStatus(repositoryId, IndexStatus.FAILED);
            syncEventService.notify(repository.getRepositoryId(), IndexStatus.FAILED);
            throw e;
        }
    }

    protected abstract void ingestRepository(final Repository repository, final UUID userId);
}
