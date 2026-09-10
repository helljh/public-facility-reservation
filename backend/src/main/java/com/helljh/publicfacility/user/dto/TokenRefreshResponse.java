package com.helljh.publicfacility.user.dto;

public record TokenRefreshResponse(

        String accessToken,
        String refreshToken,
        String tokenType

) {
}