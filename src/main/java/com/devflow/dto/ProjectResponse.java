package com.devflow.dto;


import java.time.Instant;


public class ProjectResponse {
    public record Summary(
        Long id,
        String name,
        String description,
        Boolean isPublic,
        String ownerName,
        String ownerEmail,
        Instant createdAt
    ){}
}
