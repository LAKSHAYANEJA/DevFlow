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
    
    // List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    @Query("""
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
            AND (:status IS NULL OR t.status = :status)
            AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
            ORDER BY t.createdAt DESC
            """)

            Page<Task> findByProjectIdWithFilters(
                @Param("projectId") Long projectId,
                @Param("status") TaskStatus status,
                @Param("assigneeId") Long assigneeId,
                Pageable pageable
            );
}
