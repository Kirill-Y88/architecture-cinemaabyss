package com.cinemaabyss.proxy.routing;

import lombok.Getter;

@Getter
public class RouteNotFoundException extends RuntimeException {

    private final String path;

    public RouteNotFoundException(String path) {
        super("No route configured for path " + path);
        this.path = path;
    }
}
