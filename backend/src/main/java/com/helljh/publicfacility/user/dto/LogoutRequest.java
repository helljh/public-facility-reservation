package com.helljh.publicfacility.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(

        @NotBlank
        String refreshToken

) {
}