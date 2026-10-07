package com.devflow.service;

import com.devflow.dto.AssignmentRequest;
import com.devflow.dto.TaskResponse;
import com.devflow.dto.WorkloadResponse;
import com.devflow.entity.ActivityLog;
import com.devflow.entity.Task;
import com.devflow.entity.User;
import com.devflow.repository.ActivityLogRepository;
import com.devflow.repository.ProjectMemberRepository;
import com.devflow.repository.TaskRepository;
import com.devflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogRepository activityLogRepository;
    private final TaskService taskService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET all tasks assigned to the logged-in user
    public TaskResponse.PagedResult getMyAssignedTasks(int page, int size) {
        User user = getCurrentUser();
        Page<Task> taskPage = taskRepository.findAssignedToUser(
                user.getId(), PageRequest.of(page, size));

        return new TaskResponse.PagedResult(
                taskPage.getContent().stream()
                        .map(taskService::toSummaryPublic)
                        .toList(),
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isLast()
        );
    }

    // PATCH assign a task to a member
    @Transactional
    public TaskResponse.Summary assignTask(Long taskId, AssignmentRequest.Assign request) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Long projectId = task.getProject().getId();

        // Only owner or existing member can assign
        boolean canAssign = task.getProject().getOwner().getId().equals(currentUser.getId())
                || projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!canAssign) throw new RuntimeException("Access denied");

        // Assignee must be a project member or owner
        User assignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean assigneeInProject = task.getProject().getOwner().getId().equals(assignee.getId())
                || projectMemberRepository.existsByProjectIdAndUserId(projectId, assignee.getId());
        if (!assigneeInProject) {
            throw new RuntimeException(
                "Cannot assign task to someone who is not a project member. " +
                "Invite them to the project first.");
        }

        String oldAssignee = task.getAssignee() != null
                ? task.getAssignee().getName() : "Unassigned";

        task.setAssignee(assignee);
        Task saved = taskRepository.save(task);

        // Log the assignment
        activityLogRepository.save(ActivityLog.builder()
                .task(saved)
                .actor(currentUser)
                .action("ASSIGNEE_CHANGED")
                .oldValue(oldAssignee)
                .newValue(assignee.getName())
                .build());

        return taskService.toSummaryPublic(saved);
    }

    // DELETE unassign a task
    @Transactional
    public TaskResponse.Summary unassignTask(Long taskId) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Long projectId = task.getProject().getId();
        boolean canModify = task.getProject().getOwner().getId().equals(currentUser.getId())
                || projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!canModify) throw new RuntimeException("Access denied");

        String oldAssignee = task.getAssignee() != null
                ? task.getAssignee().getName() : "Unassigned";

        task.setAssignee(null);
        Task saved = taskRepository.save(task);

        if (!oldAssignee.equals("Unassigned")) {
            activityLogRepository.save(ActivityLog.builder()
                    .task(saved)
                    .actor(currentUser)
                    .action("ASSIGNEE_CHANGED")
                    .oldValue(oldAssignee)
                    .newValue("Unassigned")
                    .build());
        }

        return taskService.toSummaryPublic(saved);
    }

    // GET workload and stats for a project
    public WorkloadResponse.ProjectStats getProjectStats(Long projectId) {
        User currentUser = getCurrentUser();

        boolean hasAccess = projectMemberRepository
                .existsByProjectIdAndUserId(projectId, currentUser.getId());
        if (!hasAccess) {
            // Also allow project owner
            hasAccess = userRepository.findById(currentUser.getId())
                    .map(u -> taskRepository.findUnassignedByProject(projectId)
                            .stream().anyMatch(t ->
                                    t.getProject().getOwner().getId().equals(currentUser.getId())))
                    .orElse(false);
        }

        // Status stats
        List<Object[]> statusRows = taskRepository.getStatusStatsByProject(projectId);
        Map<String, Long> tasksByStatus = new HashMap<>();
        long totalTasks = 0;
        for (Object[] row : statusRows) {
            String status = row[0].toString();
            Long count = (Long) row[1];
            tasksByStatus.put(status, count);
            totalTasks += count;
        }

        // Unassigned count
        long unassigned = taskRepository.findUnassignedByProject(projectId).size();

        // Workload per member
        List<Object[]> workloadRows = taskRepository.getWorkloadByProject(projectId);
        List<WorkloadResponse.MemberWorkload> workload = workloadRows.stream()
                .map(row -> new WorkloadResponse.MemberWorkload(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2]
                ))
                .toList();

        return new WorkloadResponse.ProjectStats(
                projectId,
                totalTasks,
                unassigned,
                tasksByStatus,
                workload
        );
    }
}