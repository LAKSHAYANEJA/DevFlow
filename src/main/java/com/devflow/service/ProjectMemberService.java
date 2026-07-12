package com.devflow.service;

import com.devflow.dto.MemberRequest;
import com.devflow.dto.MemberResponse;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private User getCurrentUser(){
        String email = SecurityContextHolder.getContext()
        .getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
    private void verifyOwner(Long projectId, User user){
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));

        if(!project.getOwner().getId().equals(user.getId())){
            throw new RuntimeException("Only the project owner can manage members");
        }
    }

    // ----- INVITE MEMBER -----

    @Transactional
    public MemberResponse.Summary inviteMember(Long projectId, MemberRequest.Invite request) {
        User currentUser = getCurrentUser();
        verifyOwner(projectId, currentUser);

        User invitee = userRepository.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("No user found with email "+request.email()));

        if(invitee.getId().equals(currentUser.getId())){
            throw new RuntimeException("You are already the project owner");
        }

        if(projectMemberRepository.existsByProjectIdAndUserId(projectId, invitee.getId())){
            throw new RuntimeException("User is already a member of this project");
        }

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));

        ProjectMember.ProjectMemberId memberId = new ProjectMember.ProjectMemberId(projectId, invitee.getId());


        // memberId.setProjectId(projectId);
        // memberId.setUserId(invitee.getId());

        ProjectMember member = ProjectMember.builder()
        .id(memberId).project(project).user(invitee).role(Role.MEMBER).build();

        ProjectMember saved = projectMemberRepository.save(member);
        return toSummary(saved);
    }

    // ----- LIST MEMBERS -----

    public List<MemberResponse.Summary> getMembers(Long projectId) {
        User currentUser = getCurrentUser();

        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));

        boolean hasAccess = project.getOwner().getId().equals(currentUser.getId()) || projectMemberRepository.existsByProjectIdAndUserId(projectId, currentUser.getId());

        if(!hasAccess) {
            throw new RuntimeException("Access Denied");
        }
            return projectMemberRepository.findByProjectId(projectId).stream().map(this::toSummary).toList();
        
    }

    // ----- REMOVE MEMBER -----

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        User currentUser = getCurrentUser();
        verifyOwner(projectId, currentUser);

        if(!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new RuntimeException("User is not a member of this project");
        }

        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
        }

        private MemberResponse.Summary toSummary(ProjectMember m){

            return new MemberResponse.Summary(
            m.getUser().getId(),
            m.getUser().getName(),
            m.getUser().getEmail(),
            m.getRole(),
            m.getCreatedAt()
            );
        }
}
