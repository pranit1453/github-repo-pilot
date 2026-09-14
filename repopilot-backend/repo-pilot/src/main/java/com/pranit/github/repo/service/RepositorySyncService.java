package com.pranit.github.repo.service;

import java.util.UUID;

@FunctionalInterface
public interface RepositorySyncService {

    void syncRepositories(UUID userId);
}