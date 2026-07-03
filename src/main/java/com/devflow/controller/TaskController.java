package com.devflow.controller;

import com.devflow.dto.TaskRequest;
import com.devflow.dto.TaskResponse;
import com.devflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.devflow.dto.ActivityLogResponse;


@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse.Summary> create(
        @PathVariable Long projectId,
        @Valid @RequestBody TaskRequest.Create request) {
            TaskResponse.Summary task = taskService.createTask(projectId, request);
            // return ResponseEntity.status(HttpStatus.CREATED)
            // .header("X-RateLimit-Limit", "10").
            // header("X-RateLimit-Remaining", String.valueOf(taskService.getRemainingTokens(projectId, request))).body(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, request));
        }
    @GetMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse.PagedResult> getTasksForProjects(@PathVariable Long projectId
        , @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long assigneeId
    ) {
        return ResponseEntity.ok(taskService.getTasksForProject(projectId, page, size, status, assigneeId));
    }

    @GetMapping("/api/v1/tasks/{id}")
    public ResponseEntity<TaskResponse.Summary> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTask(id));
    }

    @PatchMapping("/api/v1/tasks/{id}")
    public ResponseEntity<TaskResponse.Summary> update(
        @PathVariable Long id,
        @RequestBody TaskRequest.Update request) {
            return ResponseEntity.ok(taskService.updateTask(id, request));
        }
    
    @PatchMapping("/api/v1/tasks/{id}/status")
    public ResponseEntity<TaskResponse.Summary> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody TaskRequest.StatusUpdate request) {
            return ResponseEntity.ok(taskService.updateStatus(id, request));
        }

    @DeleteMapping("/api/v1/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/api/v1/tasks/{id}/activity")
    public ResponseEntity<List<ActivityLogResponse.Entry>> getActivity(@PathVariable Long id)
    {
        return ResponseEntity.ok(taskService.getActivity(id));
    }
    
    
    
}
