package com.devflow.repository;

import com.devflow.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.task.id = :taskId AND c.deletedAt IS NULL ORDER BY c.createdAt ASC")
    Page<Comment> findByTaskIdActive(@Param("taskId") Long taskId, Pageable pageable); 
}