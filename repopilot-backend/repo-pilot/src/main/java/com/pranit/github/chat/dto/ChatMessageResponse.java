package com.pranit.github.chat.dto;

import com.pranit.github.entities.constant.MessageRole;
import jakarta.validation.Valid;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record ChatMessageResponse(
        UUID id,
        MessageRole role,
        String content,
        @Valid
        List<CitationDto> citations,
        Instant createdAt) {
    @Builder
    public record CitationDto(
            String filePath,
            Integer startLine,
            Integer endLine,
            String language) {
    }
}