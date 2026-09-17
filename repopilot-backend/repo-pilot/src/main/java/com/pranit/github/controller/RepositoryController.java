package com.pranit.github.controller;

import com.pranit.github.helper.SecurityContext;
import com.pranit.github.repo.constant.SyncStatus;
import com.pranit.github.repo.dto.RepositoryResponse;
import com.pranit.github.repo.dto.RepositoryStatsResponse;
import com.pranit.github.repo.service.RepositoryService;
import com.pranit.github.repo.service.RepositorySyncEventService;
import com.pranit.github.repo.service.RepositorySyncService;
import com.pranit.github.wrapper.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/repository")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Repository",
        description = "APIs for synchronizing, monitoring, browsing, and retrieving the authenticated user's GitHub repositories."
)
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final RepositorySyncService syncService;
    private final RepositorySyncEventService eventService;

    @Operation(
            summary = "Synchronize GitHub repositories",
            description = "Starts synchronization of the authenticated user's GitHub repositories and returns immediately while the sync process continues."
    )
    @PostMapping(value = "/sync", version = "v1")
    public ResponseEntity<Void> syncRepositories() {
        final UUID userId = SecurityContext.getCurrentUserId();
        eventService.notify(userId, SyncStatus.INITIATED);
        syncService.syncRepositories(userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Operation(
            summary = "Subscribe to repository sync events",
            description = "Opens a Server-Sent Events stream that delivers real-time synchronization status updates for the authenticated user."
    )
    @GetMapping(value = "/sync/events", version = "v1", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribeToSync() {
        final UUID userId = SecurityContext.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.OK).body(eventService.subscribe(userId));
    }

    @Operation(
            summary = "Get repository statistics",
            description = "Returns aggregated statistics for the repositories belonging to the authenticated user."
    )
    @GetMapping(value = "/stats", version = "v1")
    public ResponseEntity<RepositoryStatsResponse> fetchStats() {
        return ResponseEntity.status(HttpStatus.OK).body(repositoryService.fetchRepositoryStats());
    }

    @Operation(
            summary = "List repositories",
            description = "Returns a paginated list of stored repositories with optional keyword filtering and configurable sorting."
    )
    @GetMapping(version = "v1")
    public ResponseEntity<PageResponse<RepositoryResponse>> fetchListOfRepositories(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        final PageResponse<RepositoryResponse> responses = repositoryService.paginatedRepositories(page, size, keyword, sortBy, sortDirection);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @Operation(
            summary = "Get repository by ID",
            description = "Retrieves the stored repository details associated with the specified repository ID."
    )
    @GetMapping(value = "/{repositoryId}", version = "v1")
    public ResponseEntity<RepositoryResponse> fetchRepositoryById(@NotNull @PathVariable UUID repositoryId) {
        return ResponseEntity.status(HttpStatus.OK).body(repositoryService.fetchStoredRepository(repositoryId));
    }
}
