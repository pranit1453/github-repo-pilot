package com.pranit.github.chat.mapper;

import com.pranit.github.chat.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Converts vector-store {@link Document}s into API citations and JSON for persistence.
 */
@Component
@RequiredArgsConstructor
public class CitationMapper {

    private final JsonMapper jsonMapper;

    private static String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Integer intVal(Object value) {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return null;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public ChatMessageResponse.CitationDto fromDocument(Document document) {
        final var meta = document.getMetadata();
        return ChatMessageResponse.CitationDto.builder()
                .filePath(stringVal(meta.get("filePath")))
                .startLine(intVal(meta.get("startLine")))
                .endLine(intVal(meta.get("endLine")))
                .language(stringVal(meta.get("language")))
                .build();
    }

    public String toJson(List<ChatMessageResponse.CitationDto> citations) {
        try {
            return jsonMapper.writeValueAsString(citations);
        } catch (JacksonException e) {
            return "[]";
        }
    }

    public List<ChatMessageResponse.CitationDto> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return jsonMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JacksonException e) {
            return List.of();
        }
    }
}
