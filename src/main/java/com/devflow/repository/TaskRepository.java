package com.devflow.repository;

import com.devflow.entity.Task;
import com.devflow.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Existing paginated query with label filter
    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN t.labels l
        WHERE t.project.id = :projectId
        AND (:status IS NULL OR t.status = :status)
        AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
        AND (:labelId IS NULL OR l.id = :labelId)
        ORDER BY t.createdAt DESC
    """)
    Page<Task> findByProjectIdWithFilters(
            @Param("projectId") Long projectId,
            @Param("status") TaskStatus status,
            @Param("assigneeId") Long assigneeId,
            @Param("labelId") Long labelId,
            Pageable pageable
    );

    // All tasks assigned to a specific user across all projects
    @Query("""
        SELECT t FROM Task t
        WHERE t.assignee.id = :userId
        AND t.deletedAt IS NULL
        ORDER BY t.createdAt DESC
    """)
    Page<Task> findAssignedToUser(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // Workload — count tasks per assignee in a project
    @Query("""
        SELECT t.assignee.id, t.assignee.name, COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
        AND t.deletedAt IS NULL
        AND t.assignee IS NOT NULL
        GROUP BY t.assignee.id, t.assignee.name
        ORDER BY COUNT(t) DESC
    """)
    List<Object[]> getWorkloadByProject(@Param("projectId") Long projectId);

    // Task stats per status for a project
    @Query("""
        SELECT t.status, COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
        AND t.deletedAt IS NULL
        GROUP BY t.status
    """)
    List<Object[]> getStatusStatsByProject(@Param("projectId") Long projectId);

    // Unassigned tasks in a project
    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId
        AND t.assignee IS NULL
        AND t.deletedAt IS NULL
        ORDER BY t.createdAt DESC
    """)
    List<Task> findUnassignedByProject(@Param("projectId") Long projectId);
}