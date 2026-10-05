package com.cinemaabyss.events.api;

import com.cinemaabyss.events.api.dto.EventEnvelope;
import com.cinemaabyss.events.api.dto.EventResponse;
import com.cinemaabyss.events.api.dto.MovieEventRequest;
import com.cinemaabyss.events.api.dto.PaymentEventRequest;
import com.cinemaabyss.events.api.dto.UserEventRequest;
import com.cinemaabyss.events.config.EventsProperties;
import com.cinemaabyss.events.kafka.EventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    public static final String SUCCESS = "success";

    private final EventPublisher publisher;
    private final EventAssembler assembler;
    private final EventsProperties properties;

    @GetMapping("/health")
    public Map<String, Boolean> health() {
        return Map.of("status", true);
    }

    @PostMapping("/movie")
    public ResponseEntity<EventResponse> createMovieEvent(@Valid @RequestBody MovieEventRequest request) {
        EventEnvelope event = assembler.assemble(EventType.MOVIE, request.movie_id(), request.action(), request);
        return created(properties.getTopics().getMovie(), String.valueOf(request.movie_id()), event);
    }

    @PostMapping("/user")
    public ResponseEntity<EventResponse> createUserEvent(@Valid @RequestBody UserEventRequest request) {
        EventEnvelope event = assembler.assemble(EventType.USER, request.user_id(), request.action(), request);
        return created(properties.getTopics().getUser(), String.valueOf(request.user_id()), event);
    }

    @PostMapping("/payment")
    public ResponseEntity<EventResponse> createPaymentEvent(@Valid @RequestBody PaymentEventRequest request) {
        EventEnvelope event = assembler.assemble(EventType.PAYMENT, request.payment_id(), request.status(), request);
        return created(properties.getTopics().getPayment(), String.valueOf(request.payment_id()), event);
    }

    private ResponseEntity<EventResponse> created(String topic, String key, EventEnvelope event) {
        EventPublisher.PublishResult result = publisher.publish(topic, key, event);
        EventResponse body = new EventResponse(SUCCESS, result.partition(), result.offset(), event);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
