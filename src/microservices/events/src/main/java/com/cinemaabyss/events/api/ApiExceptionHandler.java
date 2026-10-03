package com.cinemaabyss.events.api;

import com.cinemaabyss.events.error.EventPublishException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(ApiExceptionHandler::describe)
                .collect(Collectors.joining(", "));
        log.warn("Invalid event request: {}", message);
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Malformed event request body: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("Malformed JSON request body"));
    }

    @ExceptionHandler(EventPublishException.class)
    public ResponseEntity<ErrorResponse> handlePublishFailure(EventPublishException ex) {
        log.error("{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to publish event to Kafka"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error in the events service", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error"));
    }

    private static String describe(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    public record ErrorResponse(String error) {
    }
}
