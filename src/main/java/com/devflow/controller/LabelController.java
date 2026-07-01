package com.devflow.controller;

import com.devflow.dto.LabelRequest;
import com.devflow.dto.LabelResponse;
import com.devflow.service.LabelService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
public class LabelController {
    
    private final LabelService labelService;

    @PostMapping("/api/v1/projects/{projectId}/labels")
    public ResponseEntity<LabelResponse.Summary> create(
        @PathVariable Long projectId,
        @Valid @RequestBody LabelRequest.Create request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labelService.createLabel(projectId, request));
    }

    @GetMapping("/api/v1/projects/{projectId}/labels")
    public ResponseEntity<List<LabelResponse.Summary>> getLabels(
        @PathVariable Long projectId
    ){
        return ResponseEntity.ok(labelService.getLabelsForProject(projectId));
    }


    @PostMapping("/api/v1/tasks/{taskId}/labels/{labelId}")
    public ResponseEntity<Void> attachLabel(
        @PathVariable Long taskId,
        @PathVariable Long labelId
    )
    {
        labelService.attachLabelToTask(taskId, labelId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/tasks/{taskId}/labels/{labelId}")
    public ResponseEntity<Void> removeLabel(
        @PathVariable Long taskId,
        @PathVariable Long labelId
    ){
        labelService.removeLabelFromTask(taskId, labelId);

        return ResponseEntity.noContent().build();
    }
    
}
