package com.devflow.dto;

import com.devflow.enums.Role;
import java.time.Instant;

public class MemberResponse {
    
    public record Summary(
        Long userId,
        String name,
        String email,
        Role role,
        Instant joinedAt
    ){}
}
