package com.example.f_f.global.exception;

public class AiTimeoutException extends RuntimeException {
    public AiTimeoutException() {
        super("AI 응답 타임아웃");
    }
}