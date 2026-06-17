package com.devflow.dto;

public class AuthResponse {
    public record TokenPair(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserInfo user
    ){}

    public record UserInfo(
        Long id,
        String name,
        String email,
        String role
    ){}
}
