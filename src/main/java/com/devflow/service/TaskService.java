package com.devflow.service;

import com.devflow.dto.LabelResponse;
import com.devflow.dto.TaskRequest;
import com.devflow.dto.TaskResponse;
import com.devflow.entity.Project;
import com.devflow.entity.Task;
import com.devflow.entity.User;
import com.devflow.repository.ProjectMemberRepository;
import com.devflow.repository.ProjectRepository;
import com.devflow.repository.TaskRepository;
import com.devflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.devflow.exception.RateLimitException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devflow.enums.TaskPriority;
import com.devflow.enums.TaskStatus;
import com.devflow.exception.RateLimitException;
import com.devflow.enums.TaskStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.devflow.repository.ActivityLogRepository;
import com.devflow.entity.ActivityLog;
import com.devflow.dto.ActivityLogResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;
    private final ActivityLogRepository activityLogRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().
        getAuthentication().getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void verifyProjectAccess(Long projectId, User user) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        
        boolean hasAccess = project.getOwner().getId().equals(user.getId()) || projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId());
        
        if(!hasAccess) {
            throw new RuntimeException("Access denied to this project");
        }
    }

    // ----- CREATE TASK -----

    @Transactional
    // @CacheEvict(value = "tasks", key = "#projectId")
    public TaskResponse.Summary createTask(Long projectId, TaskRequest.Create request) {
       
        User user = getCurrentUser();

        // Rate Limit check - 10 task creations per minute per user

        if(!rateLimitService.tryConsume(user.getEmail())) {
            throw new RateLimitException(
                "Rate limit exceeded. You can create maximum 10 tasks per minute. "+ 
                "Available tokens : " + rateLimitService.getAvailableTokens(user.getEmail())
            );
        }

        verifyProjectAccess(projectId, user);

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));

        User assignee = null;

        if(request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
            .orElseThrow(() -> new RuntimeException("Assignee not found"));
        }

        Task task = Task.builder().project(project)
        .title(request.title()).description(request.description())
        .priority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM).assignee(assignee).dueDate(request.dueDate()).build();
    
        return toSummary(taskRepository.save(task));
    }

    // ----- LIST TASK FOR PROJECT -----
    // @Cacheable(value = "tasks", key = "#projectId")
    public TaskResponse.PagedResult getTasksForProject(
        Long projectId,
        int page,
        int size,
        String status,
        Long assigneeId
    ) {
        User user = getCurrentUser();
        verifyProjectAccess(projectId, user);

        TaskStatus taskStatus = null;
        if(status != null && !status.isBlank()){
            try{
                taskStatus = TaskStatus.valueOf(status.toUpperCase());
            }
            catch(IllegalArgumentException e){
                throw new RuntimeException("Invalid status : "+status);
            }
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        org.springframework.data.domain.Page<Task> taskPage = 
        taskRepository.findByProjectIdWithFilters(projectId, taskStatus, assigneeId, pageable);

        return new TaskResponse.PagedResult(
            taskPage.getContent().stream().map(this::toSummary).toList(),
            taskPage.getNumber(),
            taskPage.getSize(),
            taskPage.getTotalElements(),
            taskPage.getTotalPages(),
            taskPage.isLast()
        );
        // return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(this::toSummary).toList();
    }

    // ----- GET SINGLE TASK -----

    public TaskResponse.Summary getTask(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));

        verifyProjectAccess(task.getProject().getId(), user);
        return toSummary(task);
    }

    // ----- UPDATE TASK -----
    @Transactional
    @CacheEvict(value = "tasks", key = "#result.projectId()")
    public TaskResponse.Summary updateTask(Long taskId, TaskRequest.Update request) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));

        verifyProjectAccess(task.getProject().getId(), user);

        if(request.title() != null) task.setTitle(request.title());
        if(request.description() != null) task.setDescription(request.description());
        if(request.priority() != null) task.setPriority(request.priority());
        if(request.dueDate() != null) task.setDueDate(request.dueDate());
        if(request.prUrl() != null) task.setPrUrl(request.prUrl());

        if(request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId()).orElseThrow(() -> new RuntimeException("Assignee not found"));
            
            String oldAssignee = task.getAssignee() != null ? 
            task.getAssignee().getName() : "unassigned";
            task.setAssignee(assignee);

            activityLogRepository.save(ActivityLog.builder()
            .task(task)
            .actor(user)
            .action("ASSIGNEE_CHANGED")
            .oldValue(oldAssignee)
            .newValue(assignee.getName())
            .build());
        }

        return toSummary(taskRepository.save(task));
    }

    // ----- UPDATE STATUS -----
    @Transactional
    @CacheEvict(value = "tasks", key = "#result.projectId()")
    public TaskResponse.Summary updateStatus(Long taskId, TaskRequest.StatusUpdate request) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));

        verifyProjectAccess(task.getProject().getId(), user);

        String oldStatus = task.getStatus().name();
        task.setStatus(request.status());

        Task saved = taskRepository.save(task);

        activityLogRepository.save(ActivityLog.builder()
        .task(saved)
        .actor(user)
        .action("STATUS_CHANGED")
        .oldValue(oldStatus)
        .newValue(request.status().name())
        .build());

        return toSummary(saved);
    }

    // ----- SOFT DELETE -----

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public void deleteTask(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));

        verifyProjectAccess(task.getProject().getId(), user);

        task.softDelete();
        taskRepository.save(task);
    }



    public List<ActivityLogResponse.Entry> getActivity(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new RuntimeException("Task not found"));

        verifyProjectAccess(task.getProject().getId(), user);

        return activityLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
        .stream().
        map(log -> new ActivityLogResponse.Entry(
            log.getId(),
            log.getActor().getName(),
            log.getAction(),
            log.getOldValue(),
            log.getNewValue(),
            log.getCreatedAt()
        )).toList();
    }


    // ----- MAPPER -----

    private TaskResponse.Summary toSummary(Task t) {
        return new TaskResponse.Summary(
            t.getId(),
            t.getProject().getId(),
            t.getTitle(),
            t.getDescription(),
            t.getStatus(),
            t.getPriority(),
            t.getAssignee() != null ? t.getAssignee().getName() : null,
            t.getAssignee() != null ? t.getAssignee().getId() : null,
            t.getDueDate(),
            t.getPrUrl(),
            t.getLabels().stream().
            map(l -> new LabelResponse.Summary(
                l.getId(),
                l.getProject().getId(),
                l.getName(),
                l.getColor()
            )).toList(),
            t.getCreatedAt(),
            t.getUpdatedAt()
        );
    }
}
