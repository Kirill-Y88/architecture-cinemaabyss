package com.cinemaabyss.events.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentEventRequest(
        @NotNull(message = "is required") Integer payment_id,
        @NotNull(message = "is required") Integer user_id,
        @NotNull(message = "is required") Double amount,
        @NotBlank(message = "must not be blank") String status,
        @NotBlank(message = "must not be blank") String timestamp,
        String method_type) {
}
