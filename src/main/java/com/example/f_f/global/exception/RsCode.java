package com.example.f_f.global.exception;

import org.springframework.http.HttpStatus;

public enum RsCode {

    // ---------- 공통 ----------
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "요청 값이 올바르지 않습니다."),

    // ---------- 인증/인가 ----------
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),

    // ---------- 사용자/리소스 ----------
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM_NOT_FOUND", "존재하지 않는 채팅방입니다."),

    // ---------- Auth ----------
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "DUPLICATE_USER_ID", "이미 사용 중인 아이디입니다."),
    EMAIL_VERIFICATION_FAILED(HttpStatus.PRECONDITION_FAILED, "EMAIL_VERIFICATION_FAILED", "이메일 인증이 필요합니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),

    // ---------- Email ----------
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_CODE", "인증 코드가 올바르지 않거나 만료되었습니다."),

    // ---------- AI ----------
    AI_UPSTREAM_ERROR(HttpStatus.BAD_GATEWAY, "AI_UPSTREAM_ERROR", "외부 서비스 오류가 발생했습니다."),
    AI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI_TIMEOUT", "외부 서비스 응답이 지연되었습니다."),
    AI_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "AI_EMPTY_RESPONSE", "외부 서비스 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    RsCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
