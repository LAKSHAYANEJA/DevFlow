package com.devflow.controller;

import com.devflow.dto.AssignmentRequest;
import com.devflow.dto.TaskResponse;
import com.devflow.dto.WorkloadResponse;
import com.devflow.service.AssignmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AssignmentController {

    private final AssignmentService assignmentService;

    // My assigned tasks across all projects
    @GetMapping("/api/v1/tasks/assigned-to-me")
    public ResponseEntity<TaskResponse.PagedResult> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(assignmentService.getMyAssignedTasks(page, size));
    }

    // Assign a task to a project member
    @PatchMapping("/api/v1/tasks/{taskId}/assign")
    public ResponseEntity<TaskResponse.Summary> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody AssignmentRequest.Assign request) {
        return ResponseEntity.ok(assignmentService.assignTask(taskId, request));
    }

    // Remove assignment from a task
    @DeleteMapping("/api/v1/tasks/{taskId}/assign")
    public ResponseEntity<TaskResponse.Summary> unassignTask(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(assignmentService.unassignTask(taskId));
    }

    // Project workload and stats
    @GetMapping("/api/v1/projects/{projectId}/stats")
    public ResponseEntity<WorkloadResponse.ProjectStats> getProjectStats(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(assignmentService.getProjectStats(projectId));
    }
}