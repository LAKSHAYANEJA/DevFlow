package com.devflow.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserInfo user
) {
    public record UserInfo(
        Long id,
        String name,
        String email,
        String role
    ) {}
}