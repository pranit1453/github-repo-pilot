package com.pranit.github.chat.service.impl;

import com.pranit.github.chat.dto.ChatMessageResponse;

import java.util.List;

public record RetrievedContext(
        List<ChatMessageResponse.CitationDto> citations,
        String contextText) {
}
