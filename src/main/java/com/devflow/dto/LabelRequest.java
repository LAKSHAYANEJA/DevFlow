package com.devflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LabelRequest {
    public record Create(
        @NotBlank(message = "Label name is required")
        String name,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code like #FF5733")
        String color
    ){}
}
