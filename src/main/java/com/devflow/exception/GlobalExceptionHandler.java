package com.devflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import io.micrometer.core.ipc.http.HttpSender.Response;

import java.util.Map;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimit(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .body(Map.of("error", "Rate limit exceeded", 
            "message",ex.getMessage(), 
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).
        body(Map.of(
            "error", "Bad Request",
            "message", ex.getMessage(),
            "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler({OptimisticLockException.class, 
        ObjectOptimisticLockingFailureException.class
    }) public ResponseEntity<Map<String, Object>> handleOptimisticLock(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            Map.of(
                "error", "Confilct", 
                "message", "This task was modified by someone else. Please refresh and try again.",
                "timestamp", Instant.now().toString()
            )
        );
    }

}
