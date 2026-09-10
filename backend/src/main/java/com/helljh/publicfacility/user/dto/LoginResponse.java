package com.helljh.publicfacility.user.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long userId,
        String email,
        String name,
        String role
) {
}