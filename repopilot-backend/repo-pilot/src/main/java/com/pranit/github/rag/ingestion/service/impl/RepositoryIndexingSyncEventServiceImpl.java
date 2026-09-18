package com.pranit.github.rag.ingestion.service.impl;

import com.pranit.github.entities.constant.IndexStatus;
import com.pranit.github.rag.ingestion.service.RepositoryIndexingSyncEventService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RepositoryIndexingSyncEventServiceImpl implements RepositoryIndexingSyncEventService {

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
        } catch (IOException e) {
            removeEmitter(userId, emitter);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @Override
    public void notify(final UUID userId, final IndexStatus status) {
        final SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .name("indexing-sync")
                    .data(Map.of("status", status.name())));
            if (status == IndexStatus.INDEXED || status == IndexStatus.FAILED) {
                emitter.complete();
                emitters.remove(userId, emitter);
            }
        } catch (IOException e) {
            emitters.remove(userId, emitter);
            emitter.completeWithError(e);
        }
    }

    private void removeEmitter(final UUID userId, final SseEmitter emitter) {
        emitters.remove(userId, emitter);
    }
}
