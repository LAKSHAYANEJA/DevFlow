package com.devflow.dto;

import java.time.Instant;

public class ActivityLogResponse {
    
    public record Entry(
        Long id,
        String actorName,
        String action,
        String oldValue,
        String newValue,
        Instant createdAt
    ){}
}
