package com.cinemaabyss.proxy.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    public static final String HEALTHY_MESSAGE = "Strangler Fig Proxy is healthy";

    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(HEALTHY_MESSAGE);
    }
}
