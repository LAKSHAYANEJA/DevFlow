package com.devflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserProfileRequest {

    public record Update(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100)
        String name,

        @Size(max=500)
        String bio
    ) {}
}
