package com.cinemaabyss.events.api;

import com.cinemaabyss.events.config.EventsProperties;
import com.cinemaabyss.events.error.EventPublishException;
import com.cinemaabyss.events.kafka.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest {

    private final EventPublisher publisher = mock(EventPublisher.class);

    private final EventsProperties properties = new EventsProperties();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        when(publisher.publish(anyString(), anyString(), any()))
                .thenReturn(new EventPublisher.PublishResult(0, 42L));
        EventController controller = new EventController(publisher, new EventAssembler(new ObjectMapper()), properties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void healthReturnsTrue() throws Exception {
        mockMvc.perform(get("/api/events/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    void createsMovieEvent() throws Exception {
        mockMvc.perform(post("/api/events/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movie_id\":1,\"title\":\"Test Movie Event\",\"action\":\"viewed\",\"user_id\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.partition").value(0))
                .andExpect(jsonPath("$.offset").value(42))
                .andExpect(jsonPath("$.event.type").value("movie"))
                .andExpect(jsonPath("$.event.timestamp").exists())
                .andExpect(jsonPath("$.event.payload.movie_id").value(1))
                .andExpect(jsonPath("$.event.payload.title").value("Test Movie Event"));

        verify(publisher).publish(eq("movie-events"), eq("1"), any());
    }

    @Test
    void createsUserEvent() throws Exception {
        mockMvc.perform(post("/api/events/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user_id\":1,\"username\":\"testuser\",\"action\":\"logged_in\","
                                + "\"timestamp\":\"2026-09-30T10:00:00.000Z\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.event.type").value("user"))
                .andExpect(jsonPath("$.event.payload.action").value("logged_in"));

        verify(publisher).publish(eq("user-events"), eq("1"), any());
    }

    @Test
    void createsPaymentEvent() throws Exception {
        mockMvc.perform(post("/api/events/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"payment_id\":7,\"user_id\":1,\"amount\":9.99,\"status\":\"completed\","
                                + "\"timestamp\":\"2026-09-30T10:00:00.000Z\",\"method_type\":\"credit_card\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.event.type").value("payment"))
                .andExpect(jsonPath("$.event.payload.amount").value(9.99));

        verify(publisher).publish(eq("payment-events"), eq("7"), any());
    }

    @Test
    void rejectsEventWithoutRequiredField() throws Exception {
        mockMvc.perform(post("/api/events/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movie_id\":1,\"action\":\"viewed\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("title must not be blank"));
    }

    @Test
    void rejectsMalformedBody() throws Exception {
        mockMvc.perform(post("/api/events/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user_id\":,"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON request body"));
    }

    @Test
    void returnsServerErrorWhenKafkaIsUnavailable() throws Exception {
        when(publisher.publish(anyString(), anyString(), any()))
                .thenThrow(new EventPublishException("kafka is down", new RuntimeException("connection refused")));

        mockMvc.perform(post("/api/events/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movie_id\":1,\"title\":\"Test\",\"action\":\"viewed\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to publish event to Kafka"));
    }
}
