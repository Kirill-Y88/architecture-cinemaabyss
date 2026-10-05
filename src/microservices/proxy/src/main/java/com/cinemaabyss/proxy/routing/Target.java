package com.cinemaabyss.proxy.routing;

import lombok.Getter;

@Getter
public enum Target {

    MONOLITH("monolith"),

    MOVIES_SERVICE("movies-service"),

    EVENTS_SERVICE("events-service");

    private final String label;

    Target(String label) {
        this.label = label;
    }
}
