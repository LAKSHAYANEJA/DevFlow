package com.devflow.service;

import com.devflow.dto.LabelRequest;
import com.devflow.dto.LabelResponse;
import com.devflow.entity.Label;
import com.devflow.entity.Project;
import com.devflow.entity.Task;
import com.devflow.entity.User;
import com.devflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {
    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext()
        .getAuthentication().getName();

        return userRepository.findByEmail(email).
        orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void verifyProjectAccess(Long projectId, User user){
        Project project = projectRepository.findById(projectId).
        orElseThrow(() -> new RuntimeException("Project not found"));
        
        boolean hasAccess = project.getOwner().getId().equals(user.getId()) 
        || projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId());

        if(!hasAccess) {
            throw new RuntimeException("Access denied to this project");
        }
    }

    // ----- CREATE LABEL -----

    @Transactional
    public LabelResponse.Summary createLabel(Long projectId, LabelRequest.Create request){
        User user = getCurrentUser();
        verifyProjectAccess(projectId, user);

        Project project = projectRepository.findById(projectId).
        orElseThrow(() -> new RuntimeException("Project not found"));

        Label label = Label.builder().project(project).name(request.name()).color(request.color() != null ? request.color() : "#6B7280").build();

        return toSummary(labelRepository.save(label));
    }

    // ----- LIST LABELS FOR PROJECT -----

    public List<LabelResponse.Summary> getLabelsForProject(Long projectId){
        User user = getCurrentUser();
        verifyProjectAccess(projectId, user);

        return labelRepository.findByProjectId(projectId).stream().map(this::toSummary).toList();
    }

    // ----- ATTACH LABEL TO TASK -----

    @Transactional
    public void attachLabelToTask(Long taskId, Long labelId){
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId).
        orElseThrow(() -> new RuntimeException("Task not found"));

        verifyProjectAccess(task.getProject().getId(), user);

        Label label = labelRepository.findById(labelId).
        orElseThrow(() -> new RuntimeException("Label not found"));

        // safety check - label must belong to the same project as the task

        if(!label.getProject().getId().equals(task.getProject().getId())){
            throw new RuntimeException("Label does not belong to this task's project");
        }

        task.getLabels().add(label);
        taskRepository.save(task);
    }

    // ----- REMOVE LABEL FROM TASK -----

    @Transactional
    public void removeLabelFromTask(Long taskId, Long labelId){
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId).
        orElseThrow(() -> new RuntimeException("Task not found"));

        verifyProjectAccess(task.getProject().getId(), user);

        task.getLabels().removeIf(label -> label.getId().equals(labelId));
        taskRepository.save(task);
    }

    private LabelResponse.Summary toSummary(Label l){
        return new LabelResponse.Summary(
            l.getId(),
            l.getProject().getId(),
            l.getName(),
            l.getColor()
        );
    }

}
