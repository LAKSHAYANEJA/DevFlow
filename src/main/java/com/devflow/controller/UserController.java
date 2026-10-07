package com.devflow.controller;

import com.devflow.dto.UserProfileRequest;
import com.devflow.dto.UserProfileResponse;
import com.devflow.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse.Profile> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse.Profile> updateMyProfile(
            @Valid @RequestBody UserProfileRequest.Update request) {
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse.Profile> getUserById(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}