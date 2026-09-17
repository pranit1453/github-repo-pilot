package com.pranit.github.repo.service;

import com.pranit.github.repo.constant.SyncStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface RepositorySyncEventService {

    SseEmitter subscribe(UUID userId);

    void notify(UUID userId, SyncStatus status);
}
