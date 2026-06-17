package com.devflow.repository;

import com.devflow.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // All projects where user is owner or member

    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN p.members m
            WHERE p.owner.id = :userId
            OR m.user.id = :userId
            ORDER BY p.createdAt DESC
            """)
            List<Project> findAllByUserId(@Param("userId") Long userId);

            boolean existsByIdAndOwnerId(Long id, Long ownerId);

    
} 