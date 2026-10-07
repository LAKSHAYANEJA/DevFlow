package com.devflow.dto;

import jakarta.validation.constraints.NotNull;

public class AssignmentRequest {

    public record Assign(
        @NotNull(message = "Assignee ID is required") 
        Long assigneeId
    ) {}
}