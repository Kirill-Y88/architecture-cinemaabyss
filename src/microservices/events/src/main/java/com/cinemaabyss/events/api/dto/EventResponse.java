package com.cinemaabyss.events.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventResponse(String status, Integer partition, Long offset, EventEnvelope event) {
}
