package com.devflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequest {
    public record Register(
        @NotBlank(message = "Name is Required")
        String name,

        @Email(message = "Invalid Email")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
    ) {}

    public record Login(
        @Email @NotBlank
        String email,

        @NotBlank
        String password
    ){}
}
