package com.helljh.publicfacility.user.dto;

public record UserMeResponse(
        Long userId,
        String email,
        String name,
        String role
) {
}