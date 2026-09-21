package com.pranit.github.chat.service;

import com.pranit.github.chat.dto.ChatMessageRequest;
import com.pranit.github.chat.dto.ChatMessageResponse;
import com.pranit.github.chat.dto.ChatSessionResponse;
import com.pranit.github.chat.dto.CreateChatSessionRequest;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    ChatSessionResponse createSession(UUID userId, CreateChatSessionRequest request);

    List<ChatSessionResponse> getSessions(UUID userId, UUID repositoryId);

    List<ChatMessageResponse> getMessages(UUID userId, UUID sessionId);

    ChatMessageResponse sendMessage(UUID userId, UUID sessionId, ChatMessageRequest request);
}
