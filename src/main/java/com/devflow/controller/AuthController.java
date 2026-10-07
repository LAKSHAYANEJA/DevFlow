package com.devflow.controller;

import com.devflow.dto.AuthRequest;
import com.devflow.dto.AuthResponse;
import com.devflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody AuthRequest.Register request) {
        return ResponseEntity.status(HttpStatus.CREATED).
        body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest.Login request) {     
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
        @RequestBody Map<String, String> body
    ) {
        String refreshToken = body.get("refreshToken");
        if(refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestBody Map<String, String> body
    ) {
        String refreshToken = body.get("refreshToken");
        if(refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password") 
    public ResponseEntity<Map<String, String>> changePassword(
        @Valid @RequestBody AuthRequest.ChangePassword request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Get user id from security context
        com.devflow.entity.User user = (com.devflow.entity.User) userDetails;
        authService.changePassword(user.getId(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));

    }
    
    
}
