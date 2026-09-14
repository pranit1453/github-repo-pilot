package com.pranit.github.repo.service.impl;

import com.pranit.github.entities.entity.Repository;
import com.pranit.github.helper.SecurityContext;
import com.pranit.github.repo.dto.RepositoryResponse;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryRepository repositoryRepository;

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
