package com.devflow.dto;

import java.time.Instant;
import java.util.List;

public class CommentResponse {
    
    public record Summary(
        Long id,
        Long taskId,
        Long authorId,
        String authorName,
        String content,
        Instant createdAt,
        Instant updatedAt,
        boolean editable
    ) {}

    public record Paged(
        List<Summary> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
    ) {}
}
