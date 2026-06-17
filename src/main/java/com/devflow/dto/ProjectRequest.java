package com.devflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectRequest {
    public record Create(
        @NotBlank(message = "Project Name is required")
        @Size(max = 200, message = "Name must be under 200 characters")
        String name,

        String description,

        Boolean isPublic
    ){}

    public record Update(
        @Size(max = 200)
        String name,

        String description,

        Boolean isPublic
    ){}

}
