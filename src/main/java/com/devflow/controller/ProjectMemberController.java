package com.devflow.controller;

import com.devflow.dto.MemberRequest;
import com.devflow.dto.MemberResponse;
import com.devflow.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @PostMapping("/api/v1/projects/{projectId}/members")
    public ResponseEntity<MemberResponse.Summary> invite(
        @PathVariable Long projectId,
        @Valid @RequestBody MemberRequest.Invite request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
    .body(projectMemberService.inviteMember(projectId, request));
    }

    @GetMapping("/api/v1/projects/{projectId}/members")
    public ResponseEntity<List<MemberResponse.Summary>> getMembers(
        @PathVariable Long projectId){
            return ResponseEntity.ok(projectMemberService.getMembers(projectId));
        }

    @DeleteMapping("/api/v1/projects/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
        @PathVariable Long projectId, 
        @PathVariable Long userId
    ){
        projectMemberService.removeMember(projectId, userId);

        return ResponseEntity.noContent().build();
    }
}
