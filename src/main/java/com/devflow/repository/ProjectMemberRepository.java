package com.devflow.repository;

import com.devflow.entity.ProjectMember;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository 
    extends JpaRepository<ProjectMember, ProjectMember.ProjectMemberId>
{
        boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectMember> findByProjectId(Long projectId);

    @Modifying
    @Query("DELETE FROM ProjectMember pm WHERE pm.id.projectId = :projectId AND pm.id.userId = :userId")

    void deleteByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
} 