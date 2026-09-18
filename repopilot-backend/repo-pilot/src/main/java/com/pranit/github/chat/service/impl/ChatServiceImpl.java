package com.pranit.github.chat.service.impl;

import com.pranit.github.chat.dto.ChatMessageRequest;
import com.pranit.github.chat.dto.ChatMessageResponse;
import com.pranit.github.chat.dto.ChatSessionResponse;
import com.pranit.github.chat.dto.CreateChatSessionRequest;
import com.pranit.github.chat.exception.ChatSessionNotFoundException;
import com.pranit.github.chat.mapper.CitationMapper;
import com.pranit.github.chat.repository.ChatMessageRepository;
import com.pranit.github.chat.repository.ChatSessionRepository;
import com.pranit.github.chat.service.ChatService;
import com.pranit.github.entities.constant.MessageRole;
import com.pranit.github.entities.entity.ChatMessage;
import com.pranit.github.entities.entity.ChatSession;
import com.pranit.github.repo.dto.RepositoryResponse;
import com.pranit.github.repo.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RepositoryService repositoryService;
    private final CodeContextRetriever codeContextRetriever;
    private final ChatPromptBuilder chatPromptBuilder;
    private final CitationMapper citationMapper;
    private final ChatModel chatModel;

    @Override
    @Transactional
    public ChatSessionResponse createSession(UUID userId, CreateChatSessionRequest request) {
        log.info("Creating chat session for userId: {} and repositoryId: {}", userId, request.repositoryId());
        repositoryService.fetchStoredRepository(request.repositoryId());
        ChatSession chatSession = ChatSession.builder()
                .userId(userId)
                .repositoryId(request.repositoryId())
                .title(request.title() != null && !request.title().isBlank() ? request.title() : "New Chat")
                .build();

        ChatSession savedSession = chatSessionRepository.save(chatSession);
        return new ChatSessionResponse(
                savedSession.getId(),
                savedSession.getRepositoryId(),
                savedSession.getTitle(),
                savedSession.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatSessionResponse> getSessions(UUID userId, UUID repositoryId) {
        log.info("Fetching chat sessions for userId: {} and repositoryId: {}", userId, repositoryId);
        return chatSessionRepository.findByUserIdAndRepositoryIdOrderByCreatedAtDesc(userId, repositoryId)
                .stream()
                .map(session -> new ChatSessionResponse(
                        session.getId(),
                        session.getRepositoryId(),
                        session.getTitle(),
                        session.getCreatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(UUID userId, UUID sessionId) {
        log.info("Fetching chat messages for userId: {} and sessionId: {}", userId, sessionId);
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ChatSessionNotFoundException("Chat session not found: " + sessionId));

        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .map(msg -> new ChatMessageResponse(
                        msg.getId(),
                        msg.getRole(),
                        msg.getContent(),
                        citationMapper.fromJson(msg.getCitations()),
                        msg.getCreatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(UUID userId, UUID sessionId, ChatMessageRequest request) {
        log.info("Sending blocking chat message for userId: {} and sessionId: {}", userId, sessionId);
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ChatSessionNotFoundException("Chat session not found: " + sessionId));

        RepositoryResponse repo = repositoryService.fetchStoredRepository(session.getRepositoryId());

        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(session.getId())
                .role(MessageRole.USER)
                .content(request.content())
                .build());

        RetrievedContext retrievedContext = codeContextRetriever.retrieve(session.getRepositoryId(), request.content());
        String systemPrompt = chatPromptBuilder.systemPrompt(repo.fullName());
        String userPrompt = chatPromptBuilder.userPrompt(retrievedContext.contextText(), request.content());

        String aiResponse = ChatClient.builder(chatModel)
                .build()
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        ChatMessage assistantMsg = chatMessageRepository.save(ChatMessage.builder()
                .sessionId(session.getId())
                .role(MessageRole.ASSISTANT)
                .content(aiResponse != null ? aiResponse : "")
                .citations(citationMapper.toJson(retrievedContext.citations()))
                .build());

        return new ChatMessageResponse(
                assistantMsg.getId(),
                assistantMsg.getRole(),
                assistantMsg.getContent(),
                citationMapper.fromJson(assistantMsg.getCitations()),
                assistantMsg.getCreatedAt()
        );
    }
}
