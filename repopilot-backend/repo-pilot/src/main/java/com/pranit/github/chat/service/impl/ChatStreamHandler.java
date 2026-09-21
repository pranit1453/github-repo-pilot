package com.pranit.github.chat.service.impl;

import com.pranit.github.chat.dto.ChatMessageResponse;
import com.pranit.github.chat.mapper.CitationMapper;
import com.pranit.github.chat.repository.ChatMessageRepository;
import com.pranit.github.constant.RepoMetadata;
import com.pranit.github.entities.constant.MessageRole;
import com.pranit.github.entities.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Generation step: call OpenAI via Spring AI and stream tokens to the browser over SSE.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatStreamHandler {

    private final ChatModel chatModel;
    private final ChatMessageRepository chatMessageRepository;
    private final CitationMapper citationMapper;

    public SseEmitter stream(
            UUID sessionId, ChatMessageResponse savedUserMessage,
            List<ChatMessageResponse.CitationDto> citations,
            String systemPrompt, String userPrompt) {
        SseEmitter emitter = new SseEmitter(RepoMetadata.STREAM_TIMEOUT_MS);
        StringBuilder fullReply = new StringBuilder();
        AtomicBoolean isAborted = new AtomicBoolean(false);

        emitter.onCompletion(() -> isAborted.set(true));
        emitter.onTimeout(() -> isAborted.set(true));
        emitter.onError(e -> isAborted.set(true));

        try {
            emitter.send(SseEmitter.event()
                    .name("user_message")
                    .data(savedUserMessage));

            ChatClient.builder(chatModel)
                    .build()
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .stream()
                    .content()
                    .subscribe(
                            token -> {
                                if (!isAborted.get()) {
                                    appendToken(emitter, fullReply, token, isAborted);
                                }
                            },
                            err -> {
                                if (!isAborted.get()) {
                                    log.warn("Chat stream interrupted for session {}: {}", sessionId, err.getMessage());
                                    try {
                                        emitter.send(SseEmitter.event().name("error").data("Stream failed: " + err.getMessage()));
                                        emitter.complete();
                                    } catch (Exception ignored) {
                                    }
                                }
                            },
                            () -> {
                                if (!isAborted.get()) {
                                    completeStream(emitter, sessionId, fullReply, citations, isAborted);
                                }
                            }
                    );
        } catch (Exception ex) {
            log.warn("Error initiating chat stream: {}", ex.getMessage());
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        }

        return emitter;
    }

    private void appendToken(SseEmitter emitter, StringBuilder fullReply, String token, AtomicBoolean isAborted) {
        fullReply.append(token);
        try {
            emitter.send(SseEmitter.event()
                    .name("token")
                    .data(token, MediaType.APPLICATION_JSON));
        } catch (Exception ex) {
            log.debug("Client disconnected during chat stream token emit.");
            isAborted.set(true);
        }
    }

    private void completeStream(
            SseEmitter emitter,
            UUID sessionId,
            StringBuilder fullReply,
            List<ChatMessageResponse.CitationDto> citations,
            AtomicBoolean isAborted) {
        try {
            if (fullReply.length() > 0) {
                ChatMessage assistant = chatMessageRepository.save(ChatMessage.builder()
                        .sessionId(sessionId)
                        .role(MessageRole.ASSISTANT)
                        .content(fullReply.toString())
                        .citations(citationMapper.toJson(citations))
                        .build());

                if (!isAborted.get()) {
                    emitter.send(SseEmitter.event()
                            .name("assistant_message")
                            .data(toMessageResponse(assistant)));
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                }
            }
            if (!isAborted.get()) {
                emitter.complete();
            }
        } catch (Exception ex) {
            log.debug("Error completing chat stream: {}", ex.getMessage());
            isAborted.set(true);
        }
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                citationMapper.fromJson(message.getCitations()),
                message.getCreatedAt());
    }
}
