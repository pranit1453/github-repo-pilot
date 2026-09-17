package com.pranit.github.rag.pipeline.orchestrator;

import com.pranit.github.authentication.exception.UserNotExistsException;
import com.pranit.github.authentication.repository.UserRepository;
import com.pranit.github.client.GitHubApiClient;
import com.pranit.github.client.impl.GitHubRateLimiter;
import com.pranit.github.entities.entity.Repository;
import com.pranit.github.entities.entity.User;
import com.pranit.github.rag.pipeline.ingestion.dto.RepositoryFile;
import com.pranit.github.rag.pipeline.ingestion.filter.CodeFileFilter;
import com.pranit.github.rag.pipeline.ingestion.service.RepositoryIndexingSyncEventService;
import com.pranit.github.repo.repository.RepositoryRepository;
import lombok.extern.slf4j.Slf4j;
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

    private final GitHubApiClient gitHubApiClient;
    private final RepositoryIndexingSyncEventService syncEventService;
    private final TextEncryptor textEncryptor;
    private final UserRepository userRepository;
    private final CodeFileFilter codeFileFilter;
    private final GitHubRateLimiter gitHubRateLimiter;

    @Value("${app.indexing.max-file-bytes:102400}")
    private long maxFileBytes;

    public GithubRepositoryIngestionOrchestrator(
            RepositoryIndexingSyncEventService syncEventService,
            RepositoryRepository repositoryRepository,
            GitHubApiClient gitHubApiClient,
            TextEncryptor textEncryptor,
            UserRepository userRepository,
            CodeFileFilter codeFileFilter,
            GitHubRateLimiter gitHubRateLimiter) {
        super(repositoryRepository, syncEventService);
        this.gitHubApiClient = gitHubApiClient;
        this.syncEventService = syncEventService;
        this.textEncryptor = textEncryptor;
        this.userRepository = userRepository;
        this.codeFileFilter = codeFileFilter;
        this.gitHubRateLimiter = gitHubRateLimiter;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<RepositoryFile> fetch(final Repository repository, final UUID userId) {
        final String token = getToken(userId);
        final Map<String, Object> tree = gitHubApiClient
                .getRepoTree(token, repository.getOwner(), repository.getName(), repository.getDefaultBranch());
        if (tree == null || tree.get("tree") == null) return List.of();
        final List<Map<String, Object>> entries = (List<Map<String, Object>>) tree.get("tree");
        final List<String> filePaths = entries.stream()
                .filter(entry -> "blob".equals(String.valueOf(entry.get("type"))))
                .filter(entry -> {
                    String path = String.valueOf(entry.get("path"));
                    long size = entry.get("size") instanceof Number n ? n.longValue() : 0L;
                    return codeFileFilter.isEligible(path, size, maxFileBytes);
                })
                .map(entry -> String.valueOf(entry.get("path")))
                .toList();
        final List<RepositoryFile> files = new ArrayList<>(filePaths.size());
        for (String path : filePaths) {
            try {
                final String content = gitHubApiClient
                        .getFileContent(token, repository.getOwner(), repository.getName(), path);
                files.add(RepositoryFile.builder().path(path).content(content).build());
            } catch (Exception ex) {
                log.warn("Skipping file {} in {}: {}", path, repository.getFullName(), ex.getMessage());
            }
            gitHubRateLimiter.pause();
        }
        return files;
    }

    private String getToken(final UUID userId) {
        final User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotExistsException("User not found"));
        return textEncryptor.decrypt(user.getAccessToken());
    }

    @Override
    protected void ingest(final List<RepositoryFile> files) {

    }

    @Override
    protected void index(final Repository repository) {

    }
}
