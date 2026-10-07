package com.devflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    
    public record Create(
        @NotBlank(message = "Comment cannot be empty")
        @Size(max = 5000, message = "Comment too long")
        String content
    ) {}

    public record Update(
        @NotBlank(message = "Comment cannot be empty")
        @Size(max = 5000)
        String content
    ) {}
}
