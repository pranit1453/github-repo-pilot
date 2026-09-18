package com.pranit.github.repo.service.impl;

import com.pranit.github.repo.constant.SyncStatus;
import com.pranit.github.repo.service.RepositorySyncEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public final class RepositorySyncEventServiceImpl implements RepositorySyncEventService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(final UUID userId) {
        final SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId, emitter));
        emitter.onTimeout(() -> emitters.remove(userId, emitter));
        emitter.onError(e -> emitters.remove(userId, emitter));
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("status", "CONNECTED")));
        } catch (Exception e) {
            removeEmitter(userId, emitter);
        }
        return emitter;
    }

    @Override
    public void notify(final UUID userId, final SyncStatus status) {
        final SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .name("repository-sync")
                    .data(Map.of("status", status.name())));
            if (status == SyncStatus.COMPLETED || status == SyncStatus.FAILED) {
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                }
                emitters.remove(userId, emitter);
            }
        } catch (Exception e) {
            log.debug("SSE client disconnected or send failed for user {}: {}", userId, e.getMessage());
            emitters.remove(userId, emitter);
        }
    }

    private void removeEmitter(final UUID userId, final SseEmitter emitter) {
        emitters.remove(userId, emitter);
    }
}