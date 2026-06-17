package com.devflow.service;

import com.devflow.dto.AuthRequest;
import com.devflow.dto.AuthResponse;
import com.devflow.entity.User;
import com.devflow.enums.Role;
import com.devflow.repository.UserRepository;
import com.devflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse.TokenPair register(AuthRequest.Register request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder().
        name(request.name()).
        email(request.email()).
        passwordHash(passwordEncoder.encode(request.password())).
        role(Role.MEMBER).
        enabled(true).
        build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return buildTokenPair(accessToken, refreshToken, user);
    }

    public AuthResponse.TokenPair login(AuthRequest.Login request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return buildTokenPair(accessToken, refreshToken, user);
    }

    private AuthResponse.TokenPair buildTokenPair(String access, String refresh, User user) {
        return new AuthResponse.TokenPair(
            access,refresh,
            "Bearer",
            900L,
            new AuthResponse.UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
            )
        );
    }

}
