package com.pranit.github.repo.service;

import com.pranit.github.repo.dto.RepositoryResponse;
import com.pranit.github.wrapper.PageResponse;

import java.util.UUID;

public interface RepositoryService {

    PageResponse<RepositoryResponse> paginatedRepositories(int page, int size, String keyword, String sortBy, String sortDirection);

    RepositoryResponse fetchStoredRepository(UUID repositoryId);
}
