package com.devflow.controller;

import com.devflow.dto.ProjectRequest;
import com.devflow.dto.ProjectResponse;
import com.devflow.service.ProjectService; 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse.Summary> create(
        @Valid @RequestBody ProjectRequest.Create request) {
            return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
        }

        @GetMapping
        public ResponseEntity<List<ProjectResponse.Summary>> getMyProjects() {
            return ResponseEntity.ok(projectService.getMyProjects());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ProjectResponse.Summary> getProject(@PathVariable Long id) {
            return ResponseEntity.ok(projectService.getProject(id));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ProjectResponse.Summary> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest.Update request) {
                return ResponseEntity.ok(projectService.updateProject(id, request));
            }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Long id) {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        }
}
