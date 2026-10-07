package com.devflow.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> errorBody(int status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        body.put("requestId", UUID.randomUUID().toString().substring(0, 8));
        return body;
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(
            RateLimitException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(errorBody(429, "RATE_LIMIT_EXCEEDED", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(
            Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(409, "CONFLICT", 
                    "This resource was modified by another request. Please refresh and try again.",
                    req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            fieldErrors.put(field, error.getDefaultMessage());
        });
        Map<String, Object> body = errorBody(400, "VALIDATION_ERROR",
                "Request validation failed", req.getRequestURI());
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(
            RuntimeException ex, HttpServletRequest req) {
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";

        // Map common messages to proper status codes
        if (message.contains("not found") || message.contains("does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody(404, "NOT_FOUND", message, req.getRequestURI()));
        }
        if (message.contains("Access") || message.contains("denied") || message.contains("authorized")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorBody(403, "FORBIDDEN", message, req.getRequestURI()));
        }
        if (message.contains("already") || message.contains("duplicate") || message.contains("exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(errorBody(409, "CONFLICT", message, req.getRequestURI()));
        }
        if (message.contains("Invalid") || message.contains("expired") || message.contains("token")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorBody(401, "UNAUTHORIZED", message, req.getRequestURI()));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody(400, "BAD_REQUEST", message, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(500, "INTERNAL_SERVER_ERROR",
                        "Something went wrong. Please try again.", req.getRequestURI()));
    }
}