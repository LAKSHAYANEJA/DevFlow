package com.devflow.dto;

import java.time.Instant;

public class UserProfileResponse {
    
    public record Profile(
        Long id,
        String name,
        String email,
        String role,
        String bio,
        String avatarUrl,
        Instant createdAt
    ) {}
}
