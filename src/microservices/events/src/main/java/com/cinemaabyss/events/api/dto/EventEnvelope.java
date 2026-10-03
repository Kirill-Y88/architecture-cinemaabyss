package com.cinemaabyss.events.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventEnvelope(String id, String type, String timestamp, Map<String, Object> payload) {
}
