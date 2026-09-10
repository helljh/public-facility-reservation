package com.helljh.publicfacility.common.response;

public record ErrorResponse(
        String code,
        String message
) {
}