package com.cinemaabyss.events.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserEventRequest(
        @NotNull(message = "is required") Integer user_id,
        String username,
        String email,
        @NotBlank(message = "must not be blank") String action,
        @NotBlank(message = "must not be blank") String timestamp) {
}
