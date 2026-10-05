package com.cinemaabyss.proxy.web;

import com.cinemaabyss.proxy.routing.RouteNotFoundException;
import com.cinemaabyss.proxy.upstream.UpstreamTimeoutException;
import com.cinemaabyss.proxy.upstream.UpstreamUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RouteNotFoundException ex) {
        log.warn("{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UpstreamTimeoutException.class)
    public ResponseEntity<Map<String, String>> handleTimeout(UpstreamTimeoutException ex) {
        log.error("{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UpstreamUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleUnavailable(UpstreamUnavailableException ex) {
        log.error("{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        log.error("Unexpected proxy error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal Server Error"));
    }
}
