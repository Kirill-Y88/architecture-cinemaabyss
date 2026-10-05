package com.cinemaabyss.events.kafka;

import com.cinemaabyss.events.api.dto.EventEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "events.consumer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"${events.topics.movie}", "${events.topics.user}", "${events.topics.payment}"},
            groupId = "${events.consumer.group-id}")
    public void onEvent(ConsumerRecord<String, String> record) {
        log.info("Received event {} from topic={} partition={} offset={} key={}",
                describe(record.value()), record.topic(), record.partition(), record.offset(), record.key());
    }

    private String describe(String value) {
        try {
            EventEnvelope event = objectMapper.readValue(value, EventEnvelope.class);
            return "id=" + event.id() + " type=" + event.type() + " timestamp=" + event.timestamp()
                    + " payload=" + event.payload();
        } catch (JsonProcessingException ex) {
            return "raw=" + value;
        }
    }
}
