package com.devflow.controller;

import com.devflow.dto.TaskRequest;
import com.devflow.dto.TaskResponse;
import com.devflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse.Summary> create(
        @PathVariable Long projectId,
        @Valid @RequestBody TaskRequest.Create request) {
            return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, request));
        }
    @GetMapping("/api/v1/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse.Summary>> getTasksForProjects(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksForProject(projectId));
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
    
    
}
