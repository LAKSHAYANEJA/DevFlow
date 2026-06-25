package com.devflow.service;

import com.devflow.dto.ProjectRequest;
import com.devflow.dto.ProjectResponse;
import com.devflow.entity.Project;
import com.devflow.entity.ProjectMember;
import com.devflow.entity.User;
import com.devflow.enums.Role;
import com.devflow.repository.ProjectMemberRepository;
import com.devflow.repository.ProjectRepository;
import com.devflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    // ----- GET CURRENT USER -----

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext()
        .getAuthentication().getName();

        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

    }

    // ----- CREATE PROJECT -----

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse.Summary createProject(ProjectRequest.Create request) {
        User owner = getCurrentUser();

        Project project = Project.builder()
        .name(request.name()).
        description(request.description()).
        owner(owner).
        isPublic(request.isPublic() != null ? request.isPublic() : false)
        .build();

        project = projectRepository.save(project);

        // Auto-add owner as ADMIN member

        ProjectMember.ProjectMemberId memberId = new ProjectMember.ProjectMemberId(project.getId(), owner.getId());

        ProjectMember member = ProjectMember.builder().
        id(memberId).
        project(project).
        user(owner).
        role(Role.ADMIN)
        .build();

        projectMemberRepository.save(member);

        return toSummary(project);
    }

    // ----- GET ALL PROJECTS FOR USER -----

    @Cacheable(value = "projects", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public List<ProjectResponse.Summary> getMyProjects() {
        User user = getCurrentUser();

        return projectRepository.findAllByUserId(user.getId()).
        stream().map(this::toSummary).toList();
    }

    @Cacheable(value = "project", key = "#id")
    public ProjectResponse.Summary getProject(Long id) {
        User user = getCurrentUser();
        Project project = projectRepository.findById(id).
        orElseThrow(() -> new RuntimeException("Project not found"));

        // Check access - owner or member 

        boolean hasAccess = project.getOwner().getId().equals(user.getId()) || projectMemberRepository.existsByProjectIdAndUserId(id, user.getId());

        if(!hasAccess && !project.getIsPublic()){
            throw new RuntimeException("Access Denied");
        }

        return toSummary(project);
    }

    // ----- Update Project -----

    @Transactional
    // @CacheEvict({"project", "projects"}, key = "#id", allEntries = false)
    @Caching(evict = { 
    @CacheEvict(value = "project", key = "#id"),
    @CacheEvict(value = "projects", allEntries = true)
    })
    public ProjectResponse.Summary updateProject(Long id, ProjectRequest.Update request) {
        User user = getCurrentUser();
        Project project = projectRepository.findById(id).
        orElseThrow(() -> new RuntimeException("Project not found"));

        if(!project.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Only the owner can update the project");
        }

        if(request.name() != null) project.setName(request.name());
        if(request.description() != null) project.setDescription(request.description());
        if(request.isPublic() != null) project.setIsPublic(request.isPublic());

        return toSummary(projectRepository.save(project));
    }

    // ----- Delete Project -----

    @Transactional
    @Caching( evict = {
        @CacheEvict(value = "project", key = "#id"),
        @CacheEvict(value = "projects", allEntries = true)
    })
    public void deleteProject(Long id){
        User user = getCurrentUser();
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));

        if(!project.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Only the owner can delete the project");
        }

        projectRepository.delete(project);
    }

    // ----- MAPPER -----

    private ProjectResponse.Summary toSummary(Project p) {
        return new ProjectResponse.Summary(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getIsPublic(),
            p.getOwner().getName(),
            p.getOwner().getEmail(),
            p.getCreatedAt()
        );
    }

}
