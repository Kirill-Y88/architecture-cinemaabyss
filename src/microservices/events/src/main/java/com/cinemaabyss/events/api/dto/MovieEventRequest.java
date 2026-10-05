package com.cinemaabyss.events.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MovieEventRequest(
        @NotNull(message = "is required") Integer movie_id,
        @NotBlank(message = "must not be blank") String title,
        @NotBlank(message = "must not be blank") String action,
        Integer user_id,
        Double rating,
        List<String> genres,
        String description) {
}
