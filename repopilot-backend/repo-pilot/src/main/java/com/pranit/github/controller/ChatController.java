package com.pranit.github.controller;

import com.pranit.github.chat.dto.ChatMessageRequest;
import com.pranit.github.chat.dto.ChatMessageResponse;
import com.pranit.github.chat.dto.ChatSessionResponse;
import com.pranit.github.chat.dto.CreateChatSessionRequest;
import com.pranit.github.chat.service.ChatService;
import com.pranit.github.helper.SecurityContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Chat",
        description = "APIs for managing chat sessions, retrieving chat history, and streaming AI responses with codebase context in real time."
)
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "Create chat session",
            description = "Creates a new chat session associated with the specified repository for the authenticated user."
    )
    @PostMapping(value = "/sessions", version = "v1")
    public ResponseEntity<ChatSessionResponse> createSession(@Valid @RequestBody CreateChatSessionRequest request) {
        final UUID userId = SecurityContext.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createSession(userId, request));
    }

    @Operation(
            summary = "Get chat sessions",
            description = "Retrieves all active chat sessions belonging to the authenticated user for a specific repository."
    )
    @GetMapping(value = "/sessions", version = "v1")
    public ResponseEntity<List<ChatSessionResponse>> getSessions(@RequestParam @NotNull UUID repositoryId) {
        final UUID userId = SecurityContext.getCurrentUserId();
        return ResponseEntity.ok(chatService.getSessions(userId, repositoryId));
    }

    @Operation(
            summary = "Get chat messages",
            description = "Retrieves all historic chat messages (user questions, AI responses, and citations) for the given chat session."
    )
    @GetMapping(value = "/sessions/{sessionId}/messages", version = "v1")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@NotNull @PathVariable UUID sessionId) {
        final UUID userId = SecurityContext.getCurrentUserId();
        return ResponseEntity.ok(chatService.getMessages(userId, sessionId));
    }

    @Operation(
            summary = "Send chat message (blocking)",
            description = "Submits a user query for the chat session synchronously and returns the complete AI message response with citations."
    )
    @PostMapping(value = "/sessions/{sessionId}/messages", version = "v1")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @NotNull @PathVariable UUID sessionId,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        final UUID userId = SecurityContext.getCurrentUserId();
        return ResponseEntity.ok(chatService.sendMessage(userId, sessionId, request));
    }
}
