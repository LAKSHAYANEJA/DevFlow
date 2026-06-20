package com.devflow.dto;

import com.devflow.enums.TaskPriority;
import com.devflow.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


public class TaskRequest {
    public record Create(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        TaskPriority priority,

        Long assigneeId,

        LocalDate dueDate
    ) {}

    public record Update(
        String title,
        String description,
        TaskPriority priority,
        Long assigneeId,
        LocalDate dueDate,
        String prUrl
    ){}

    public record StatusUpdate(@NotNull(message = "Status is required")
    TaskStatus status
    ){}
}
