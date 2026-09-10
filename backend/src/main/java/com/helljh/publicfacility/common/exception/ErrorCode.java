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
    ),

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "AUTH_004",
            "인증이 필요합니다."
    ),

    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "AUTH_005",
            "접근 권한이 없습니다."
    ),

    FACILITY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
        "FACILITY_001",
                "시설을 찾을 수 없습니다."
    ),

    ROOM_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "ROOM_001",
            "회의실을 찾을 수 없습니다."
    ),

    INVALID_RESERVATION_DATE(
            HttpStatus.BAD_REQUEST,
        "RESERVATION_001",
                "예약 가능한 날짜가 아닙니다."
    ),

    FACILITY_CLOSED(
            HttpStatus.CONFLICT,
        "FACILITY_002",
                "해당 날짜는 시설 휴무일입니다."
    ),

    INVALID_RESERVATION_TIME(
            HttpStatus.BAD_REQUEST,
            "RESERVATION_002",
            "예약 시간이 올바르지 않습니다."
    ),

    RESERVATION_TIME_CONFLICT(
            HttpStatus.CONFLICT,
            "RESERVATION_003",
            "이미 예약된 시간입니다."
    ),

    EXCEED_ROOM_CAPACITY(
            HttpStatus.BAD_REQUEST,
            "RESERVATION_004",
            "회의실 수용 인원을 초과했습니다."
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