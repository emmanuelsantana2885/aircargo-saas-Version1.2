package com.aircargo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScanEventListener {

    private static final Logger log = LoggerFactory.getLogger(ScanEventListener.class);

    private final ConcurrentHashMap<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(UUID flightId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.put(flightId, emitter);
        emitter.onCompletion(() -> emitters.remove(flightId));
        emitter.onTimeout(() -> emitters.remove(flightId));
        emitter.onError(e -> emitters.remove(flightId));
        return emitter;
    }

    public void broadcastScanEvent(UUID flightId, String event, String data) {
        SseEmitter emitter = emitters.get(flightId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event()
                    .name(event)
                    .data(data));
        } catch (IOException e) {
            log.warn("Failed to send SSE event to flight {}: {}", flightId, e.getMessage());
            emitters.remove(flightId);
        }
    }
}
