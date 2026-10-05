package com.cinemaabyss.events.api;

import com.cinemaabyss.events.api.dto.EventEnvelope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventAssembler {

    private final ObjectMapper objectMapper;

    public EventEnvelope assemble(EventType type, Object key, String action, Object payloadSource) {
        Map<String, Object> payload = objectMapper.convertValue(payloadSource, new TypeReference<>() {
        });
        return new EventEnvelope(buildId(type, key, action), type.getLabel(), now(), payload);
    }

    private static String buildId(EventType type, Object key, String action) {
        StringBuilder id = new StringBuilder(type.getLabel());
        if (key != null) {
            id.append('-').append(key);
        }
        if (action != null && !action.isBlank()) {
            id.append('-').append(action);
        }
        return id.append('-').append(UUID.randomUUID().toString(), 0, 8).toString();
    }

    private static String now() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS).toString();
    }
}
