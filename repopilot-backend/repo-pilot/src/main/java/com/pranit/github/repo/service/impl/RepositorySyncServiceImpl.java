package com.pranit.github.repo.service.impl;

import com.pranit.github.authentication.exception.UserNotExistsException;
import com.pranit.github.authentication.repository.UserRepository;
import com.pranit.github.client.GitHubApiClient;
import com.pranit.github.entities.entity.Repository;
import com.pranit.github.entities.entity.User;
import com.pranit.github.repo.constant.SyncStatus;
import com.pranit.github.repo.repository.RepositoryRepository;
import com.pranit.github.repo.service.RepositorySyncEventService;
import com.pranit.github.repo.service.RepositorySyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositorySyncServiceImpl implements RepositorySyncService {

    private final TextEncryptor textEncryptor;
    private final GitHubApiClient gitHubApiClient;
    private final RepositoryRepository repositoryRepository;
    private final UserRepository userRepository;
    private final RepositorySyncEventService eventService;

    @Override
    @Async("syncExecutor")
    @Transactional
    public void syncRepositories(final UUID userId) {
        try {
            eventService.notify(userId, SyncStatus.PROCESSING);
            final User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new UserNotExistsException("User Not Found"));
            final String token = textEncryptor.decrypt(user.getAccessToken());
            final List<Map<String, Object>> remoteRepositories = gitHubApiClient.listUserRepos(token);
            for (Map<String, Object> remote : remoteRepositories) {
                syncRepository(userId, remote);
            }
            eventService.notify(userId, SyncStatus.COMPLETED);
        } catch (Exception e) {
            eventService.notify(userId, SyncStatus.FAILED);
            log.error("Repository sync failed for user {}", userId, e);
        }
    }

    private void syncRepository(final UUID userId, final Map<String, Object> remote) {
        final Long githubRepoId = Optional.ofNullable(remote.get("id"))
                .map(value -> ((Number) value).longValue())
                .orElseThrow(() -> new OAuth2AuthenticationException("GitHub repo id not found"));
        repositoryRepository.findByUserIdAndGithubRepoId(userId, githubRepoId)
                .map(existing -> updateRepository(existing, remote))
                .orElseGet(() -> createRepository(remote, userId, githubRepoId));
    }

    private Repository updateRepository(final Repository existing, final Map<String, Object> remote) {
        final String fullName = String.valueOf(remote.get("full_name"));
        final String[] parts = fullName.split("/", 2);
        String owner = parts.length > 0 ? parts[0] : null;
        if (owner == null || owner.isBlank()) {
            final Object ownerObj = remote.get("owner");
            if (ownerObj instanceof Map<?, ?> ownerMap && ownerMap.get("login") != null) {
                owner = String.valueOf(ownerMap.get("login"));
            }
        }
        existing.setOwner(owner);
        existing.setName(parts.length > 1 ? parts[1] : String.valueOf(remote.get("name")));
        existing.setFullName(fullName);
        existing.setPrivate(Boolean.TRUE.equals(remote.get("private")));
        existing.setDefaultBranch(remote.get("default_branch") != null
                ? String.valueOf(remote.get("default_branch"))
                : "main");
        existing.setLanguage(remote.get("language") != null ? String.valueOf(remote.get("language")) : null);
        existing.setHtmlUrl(remote.get("html_url") != null ? String.valueOf(remote.get("html_url")) : null);
        existing.setDescription(remote.get("description") != null ? String.valueOf(remote.get("description")) : null);
        return repositoryRepository.save(existing);
    }

    private Repository createRepository(final Map<String, Object> remote, final UUID userId, final Long githubRepoId) {
        final String fullName = String.valueOf(remote.get("full_name"));
        final String[] parts = fullName.split("/", 2);
        String owner = parts.length > 0 ? parts[0] : null;
        if (owner == null || owner.isBlank()) {
            final Object ownerObj = remote.get("owner");
            if (ownerObj instanceof Map<?, ?> ownerMap && ownerMap.get("login") != null) {
                owner = String.valueOf(ownerMap.get("login"));
            }
        }
        final Repository repository = Repository.builder()
                .userId(userId)
                .githubRepoId(githubRepoId)
                .owner(owner)
                .name(parts.length > 1 ? parts[1] : String.valueOf(remote.get("name")))
                .fullName(fullName)
                .isPrivate(Boolean.TRUE.equals(remote.get("private")))
                .defaultBranch(remote.get("default_branch") != null
                        ? String.valueOf(remote.get("default_branch"))
                        : "main")
                .language(remote.get("language") != null ? String.valueOf(remote.get("language")) : null)
                .htmlUrl(remote.get("html_url") != null ? String.valueOf(remote.get("html_url")) : null)
                .description(remote.get("description") != null ? String.valueOf(remote.get("description")) : null)
                .build();
        return repositoryRepository.save(repository);
    }

}
