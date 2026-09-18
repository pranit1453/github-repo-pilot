package com.pranit.github.rag.orchestrator;

import com.pranit.github.authentication.exception.UserNotExistsException;
import com.pranit.github.authentication.repository.UserRepository;
import com.pranit.github.client.GitHubApiClient;
import com.pranit.github.client.impl.GitHubRateLimiter;
import com.pranit.github.entities.constant.IndexStatus;
import com.pranit.github.entities.entity.Repository;
import com.pranit.github.entities.entity.User;
import com.pranit.github.rag.ingestion.chunker.CodeChunker;
import com.pranit.github.rag.ingestion.filter.CodeFileFilter;
import com.pranit.github.rag.ingestion.service.RepositoryIndexingSyncEventService;
import com.pranit.github.repo.repository.RepositoryRepository;
import com.pranit.github.repo.service.RepositoryIndexProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class GithubRepositoryIngestionOrchestrator extends IngestionOrchestrator {

    private static final int PROGRESS_EVERY_N_FILES = 10;

    private final GitHubApiClient gitHubApiClient;
    private final RepositoryIndexingSyncEventService syncEventService;
    private final TextEncryptor textEncryptor;
    private final UserRepository userRepository;
    private final RepositoryIndexProgressService progressService;
    private final CodeFileFilter codeFileFilter;
    private final GitHubRateLimiter gitHubRateLimiter;
    private final CodeChunker codeChunker;
    private final VectorStore vectorStore;

    @Value("${app.indexing.max-file-bytes:102400}")
    private long maxFileBytes;

    @Value("${app.indexing.vector-batch-size:32}")
    private int vectorBatchSize;

    public GithubRepositoryIngestionOrchestrator(
            RepositoryIndexingSyncEventService syncEventService,
            RepositoryRepository repositoryRepository,
            RepositoryIndexProgressService progressService,
            GitHubApiClient gitHubApiClient,
            TextEncryptor textEncryptor,
            UserRepository userRepository,
            CodeFileFilter codeFileFilter,
            GitHubRateLimiter gitHubRateLimiter,
            CodeChunker codeChunker,
            VectorStore vectorStore) {

        super(repositoryRepository, syncEventService);
        this.gitHubApiClient = gitHubApiClient;
        this.syncEventService = syncEventService;
        this.textEncryptor = textEncryptor;
        this.userRepository = userRepository;
        this.codeFileFilter = codeFileFilter;
        this.gitHubRateLimiter = gitHubRateLimiter;
        this.codeChunker = codeChunker;
        this.vectorStore = vectorStore;
        this.progressService = progressService;
    }

    @Override
    public void ingestRepository(final Repository repository, final UUID userId) {
        syncEventService.notify(repository.getRepositoryId(), IndexStatus.INDEXING);
        final String token = getToken(userId);
        final Map<String, Object> tree = gitHubApiClient.getRepoTree
                (token, repository.getOwner(), repository.getName(), repository.getDefaultBranch());
        gitHubRateLimiter.pause();
        if (tree == null || tree.get("tree") == null) {
            log.info("No files found for repository {}", repository.getFullName());
            progressService.updateProgress(repository.getRepositoryId(), 0, 0, 0);
            return;
        }
        @SuppressWarnings("unchecked") final List<Map<String, Object>> entries = (List<Map<String, Object>>) tree.get("tree");
        final List<String> filePaths = entries.stream()
                .filter(this::isBlob)
                .filter(this::isEligible)
                .map(entry -> String.valueOf(entry.get("path")))
                .toList();

        final int totalFiles = filePaths.size();
        progressService.updateProgress(repository.getRepositoryId(), totalFiles, 0, 0);
        final List<Document> batch = new ArrayList<>(vectorBatchSize);
        int processedFiles = 0;
        int indexedChunks = 0;
        for (String path : filePaths) {
            try {
                String content = gitHubApiClient.getFileContent
                        (token, repository.getOwner(), repository.getName(), path);
                gitHubRateLimiter.pause();
                if (content == null || content.isBlank()) continue;
                syncEventService.notify(repository.getRepositoryId(), IndexStatus.CHUNKING);
                List<Document> chunks = codeChunker.chunkFile
                        (repository.getRepositoryId().toString(), path, content);
                if (chunks != null) {
                    for (Document chunk : chunks) {
                        batch.add(chunk);
                        if (batch.size() >= vectorBatchSize) {
                            vectorStore.add(batch);
                            indexedChunks += batch.size();
                            batch.clear();
                            progressService.updateProgress(repository.getRepositoryId(), totalFiles, processedFiles, indexedChunks);
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("Failed to process file {} in {}: {}", path, repository.getFullName(), ex.getMessage(), ex);
            } finally {
                processedFiles++;
                if (processedFiles % PROGRESS_EVERY_N_FILES == 0 || processedFiles == totalFiles) {
                    progressService.updateProgress(repository.getRepositoryId(), totalFiles, processedFiles, indexedChunks);
                }
            }
        }
        if (!batch.isEmpty()) {
            vectorStore.add(batch);
            indexedChunks += batch.size();
            batch.clear();
        }
        progressService.updateProgress(repository.getRepositoryId(), totalFiles, processedFiles, indexedChunks);
        log.info("Repository indexing completed. repository={}, files={}, chunks={}",
                repository.getFullName(), processedFiles, indexedChunks);
    }

    private boolean isBlob(Map<String, Object> entry) {
        return "blob".equals(String.valueOf(entry.get("type")));
    }

    private boolean isEligible(Map<String, Object> entry) {
        String path = String.valueOf(entry.get("path"));
        long size = entry.get("size") instanceof Number n ? n.longValue() : 0L;
        return codeFileFilter.isEligible(path, size, maxFileBytes);
    }

    private String getToken(final UUID userId) {
        final User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotExistsException("User not found"));
        return textEncryptor.decrypt(user.getAccessToken());
    }
}