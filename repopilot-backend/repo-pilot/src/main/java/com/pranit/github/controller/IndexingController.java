package com.pranit.github.controller;

import com.pranit.github.helper.SecurityContext;
import com.pranit.github.rag.ingestion.dto.IndexingResponse;
import com.pranit.github.rag.ingestion.service.RepositoryIndexingService;
import com.pranit.github.rag.ingestion.service.RepositoryIndexingSyncEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Repository Indexing",
        description = "APIs for indexing repositories and monitoring repository indexing progress in real time."
)
public class IndexingController {

    private final RepositoryIndexingService repositoryIndexingService;
    private final RepositoryIndexingSyncEventService repositoryIndexingSyncEventService;

    @Operation(
            summary = "Start repository indexing",
            description = "Starts the indexing process for the specified repository and returns the initial indexing status."
    )
    @PostMapping(value = "/{repositoryId}/index", version = "v1")
    public ResponseEntity<IndexingResponse> startIndexing(@NotNull @PathVariable UUID repoId) {
        UUID userId = SecurityContext.getCurrentUserId();
        return ResponseEntity.accepted().body(repositoryIndexingService.startIndexing(repoId, userId));
    }

    @Operation(
            summary = "Subscribe to indexing events",
            description = "Opens a Server-Sent Events stream to receive real-time indexing status and progress updates for the specified repository."
    )
    @GetMapping(value = "/{repositoryId}/indexing/events", version = "v1", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> events(@NotNull @PathVariable UUID repoId) {
        return ResponseEntity.accepted().body(repositoryIndexingSyncEventService.subscribe(repoId));
    }
}
