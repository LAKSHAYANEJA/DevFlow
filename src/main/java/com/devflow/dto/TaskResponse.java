package com.devflow.dto;

import com.devflow.enums.TaskPriority;
import com.devflow.enums.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public class TaskResponse {

    public record Summary(
    Long id,
    Long projectId,
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    String assigneeName,
    Long assigneeId,
    LocalDate dueDate,
    String prUrl,
    Instant createdAt,
    Instant updatedAt
    ){}
}
