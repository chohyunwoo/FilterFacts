package com.example.f_f.global.exception;

public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException() {
        super("인증 코드가 올바르지 않거나 만료되었습니다.");
    }

    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}