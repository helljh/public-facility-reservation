package com.helljh.publicfacility.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_INPUT_VALUE(
            HttpStatus.BAD_REQUEST,
            "COMMON_001",
            "잘못된 요청입니다."
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_001",
            "사용자를 찾을 수 없습니다."
    ),

    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "USER_002",
            "이미 사용 중인 이메일입니다."
    ),

    INVALID_LOGIN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_001",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_002",
            "유효하지 않은 Refresh Token입니다."
    ),

    EXPIRED_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH_003",
            "만료된 Refresh Token입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(
            HttpStatus status,
            String code,
            String message
    ) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}