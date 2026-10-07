package com.devflow.service;

import com.devflow.dto.AuthRequest;
import com.devflow.dto.AuthResponse;
import com.devflow.entity.RefreshToken;
import com.devflow.entity.User;
import com.devflow.enums.Role;
import com.devflow.repository.RefreshTokenRepository;
import com.devflow.repository.UserRepository;
import com.devflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Transactional
    public AuthResponse register(AuthRequest.Register request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.MEMBER)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    @Transactional
    public AuthResponse login(AuthRequest.Login request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Revoke all existing refresh tokens for this user
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        // Revoke old token and issue new ones
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(refreshToken.getUser());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Revoke all refresh tokens — force re-login after password change
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    private AuthResponse buildAuthResponse(User user) {
    String accessToken = jwtService.generateAccessToken(user);
    String refreshTokenValue = jwtService.generateRefreshToken(user);

    // Build and persist refresh token
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setToken(refreshTokenValue);
    refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiryMs));
    refreshToken.setRevoked(false);
    refreshTokenRepository.save(refreshToken);

    return new AuthResponse(
            accessToken,
            refreshTokenValue,
            "Bearer",
            jwtService.getAccessTokenExpiry(),
            new AuthResponse.UserInfo(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().name()
            )
    );
}
}