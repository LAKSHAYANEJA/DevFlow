package com.devflow.controller;

import com.devflow.dto.CommentRequest;
import com.devflow.dto.CommentResponse;
import com.devflow.service.CommentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/v1/tasks/{taskId}/comments")
    public ResponseEntity<CommentResponse.Summary> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(taskId, request));
    }

    @GetMapping("/api/v1/tasks/{taskId}/comments")
    public ResponseEntity<CommentResponse.Paged> getComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(commentService.getComments(taskId, page, size));
    }

    @PatchMapping("/api/v1/comments/{id}")
    public ResponseEntity<CommentResponse.Summary> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest.Update request) {
        return ResponseEntity.ok(commentService.updateComment(id, request));
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}