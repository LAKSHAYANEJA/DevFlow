package com.devflow.repository;

import com.devflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    @Query("""
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
            AND (:status IS NULL OR t.status = :status)
            AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
            ORDER BY t.createdAt DESC
            """)

            List<Task> findByProjectIdWithFilters(
                @Param("projectId") Long projectId,
                @Param("status") com.devflow.enums.TaskStatus status,
                @Param("assigneeId") Long assigneeId
            );
}
