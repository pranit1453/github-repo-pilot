package com.pranit.github.rag.ingestion.service;

import com.pranit.github.entities.constant.IndexStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface RepositoryIndexingSyncEventService {

    SseEmitter subscribe(UUID userId);

    void notify(UUID userId, IndexStatus status);
}
