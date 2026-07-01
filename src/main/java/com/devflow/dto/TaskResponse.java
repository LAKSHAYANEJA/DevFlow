package com.devflow.dto;

import com.devflow.enums.TaskPriority;
import com.devflow.enums.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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
    List<LabelResponse.Summary> labels,
    Instant createdAt,
    Instant updatedAt
    ){}

    public record PagedResult(
        List<Summary> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
    ){}
}
