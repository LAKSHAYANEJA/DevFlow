package com.devflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class MemberRequest {

    public record Invite(
        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        String email
    ){}
    
}
