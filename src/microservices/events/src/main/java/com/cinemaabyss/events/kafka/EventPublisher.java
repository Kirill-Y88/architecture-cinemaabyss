package com.cinemaabyss.events.kafka;

import com.cinemaabyss.events.api.dto.EventEnvelope;
import com.cinemaabyss.events.config.EventsProperties;
import com.cinemaabyss.events.error.EventPublishException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final EventsProperties properties;

    public PublishResult publish(String topic, String key, EventEnvelope event) {
        String payload = serialize(event);
        try {
            SendResult<String, String> result = kafkaTemplate.send(topic, key, payload)
                    .get(properties.getSendTimeout().toMillis(), TimeUnit.MILLISECONDS);
            RecordMetadata metadata = result.getRecordMetadata();
            log.info("Published event id={} type={} to topic={} partition={} offset={}",
                    event.id(), event.type(), topic, metadata.partition(), metadata.offset());
            return new PublishResult(metadata.partition(), metadata.offset());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Interrupted while publishing event " + event.id(), ex);
        } catch (ExecutionException | TimeoutException ex) {
            log.error("Failed to publish event id={} to topic {}: {}", event.id(), topic, ex.toString());
            throw new EventPublishException("Failed to publish event " + event.id() + " to topic " + topic, ex);
        }
    }

    private String serialize(EventEnvelope event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new EventPublishException("Failed to serialize event " + event.id(), ex);
        }
    }

    public record PublishResult(int partition, long offset) {
    }
}
