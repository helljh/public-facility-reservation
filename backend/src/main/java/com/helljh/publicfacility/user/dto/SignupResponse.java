package com.helljh.publicfacility.user.dto;

public record SignupResponse(
        Long userId,
        String email,
        String name
) {
}