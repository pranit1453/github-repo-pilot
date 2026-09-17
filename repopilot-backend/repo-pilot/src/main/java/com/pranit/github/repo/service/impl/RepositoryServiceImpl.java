package com.pranit.github.repo.service.impl;

import com.pranit.github.entities.constant.IndexStatus;
import com.pranit.github.entities.entity.Repository;
import com.pranit.github.helper.SecurityContext;
import com.pranit.github.repo.dto.RepositoryResponse;
import com.pranit.github.repo.dto.RepositoryStatsResponse;
import com.pranit.github.repo.exception.RepositoryNotFoundException;
import com.pranit.github.repo.repository.RepositoryRepository;
import com.pranit.github.repo.service.RepositoryService;
import com.pranit.github.repo.specification.RepositorySpecification;
import com.pranit.github.wrapper.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryRepository repositoryRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public RepositoryStatsResponse fetchRepositoryStats() {
        final UUID userId = SecurityContext.getCurrentUserId();
        final List<Repository> repositories = repositoryRepository.findByUserId(userId);

        long pending = 0;
        long indexing = 0;
        long completed = 0;
        long failed = 0;
        long totalFilesProcessed = 0;
        long totalChunks = 0;
        final Map<String, Long> languages = new HashMap<>();

        for (Repository repo : repositories) {
            if (repo.getIndexStatus() != null) {
                switch (repo.getIndexStatus()) {
                    case PENDING -> pending++;
                    case IN_PROGRESS -> indexing++;
                    case COMPLETED -> completed++;
                    case FAILED -> failed++;
                }
            } else {
                pending++;
            }
            totalFilesProcessed += repo.getFilesProcessed();
            totalChunks += repo.getChunkCount();
            final String lang = repo.getLanguage();
            if (lang != null && !lang.isBlank()) {
                languages.merge(lang, 1L, Long::sum);
            }
        }

        return RepositoryStatsResponse.builder()
                .totalRepositories(repositories.size())
                .pendingIndexCount(pending)
                .indexingCount(indexing)
                .completedIndexCount(completed)
                .failedIndexCount(failed)
                .totalFilesProcessed(totalFilesProcessed)
                .totalChunks(totalChunks)
                .languages(languages)
                .build();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public PageResponse<RepositoryResponse> paginatedRepositories(
            int page, int size, String keyword, String sortBy, String sortDirection) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        final Sort sort = Sort.by(direction, sortBy);
        final Pageable pageable = PageRequest.of(page, size, sort);
        final Specification<Repository> specification = RepositorySpecification.searchKeywordAndUserId(userId, keyword);
        final Page<Repository> pages = repositoryRepository.findAll(specification, pageable);
        final List<RepositoryResponse> content = pages.getContent()
                .stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.<RepositoryResponse>builder()
                .contents(content)
                .currentPage(pages.getNumber())
                .pageSize(pages.getSize())
                .totalElements(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .isLastPage(pages.isLast())
                .isFirstPage(pages.isFirst())
                .build();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public RepositoryResponse fetchStoredRepository(final UUID repositoryId) {
        final UUID userId = SecurityContext.getCurrentUserId();
        final Repository repository = fetchAndValidateRepositoryById(repositoryId, userId);
        return toResponse(repository);
    }

    private Repository fetchAndValidateRepositoryById(final UUID repositoryId, final UUID userId) {
        return repositoryRepository.findByRepositoryIdAndUserId(repositoryId, userId)
                .orElseThrow(() -> new RepositoryNotFoundException("Repository not found"));
    }

    private RepositoryResponse toResponse(final Repository repository) {
        return RepositoryResponse.builder()
                .id(repository.getRepositoryId())
                .githubRepoId(repository.getGithubRepoId())
                .owner(repository.getOwner())
                .name(repository.getName())
                .fullName(repository.getFullName())
                .isPrivate(repository.isPrivate())
                .defaultBranch(repository.getDefaultBranch())
                .language(repository.getLanguage())
                .htmlUrl(repository.getHtmlUrl())
                .description(repository.getDescription())
                .indexStatus(repository.getIndexStatus())
                .indexedAt(repository.getIndexedAt())
                .chunkCount(repository.getChunkCount())
                .filesTotal(repository.getFilesTotal())
                .filesProcessed(repository.getFilesProcessed())
                .build();
    }
}
