package com.cinemaabyss.events.api;

import lombok.Getter;

@Getter
public enum EventType {

    MOVIE("movie"),
    USER("user"),
    PAYMENT("payment");

    private final String label;

    EventType(String label) {
        this.label = label;
    }
}
