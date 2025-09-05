package com.example.f_f.global.exception;

public class AiEmptyResponseException extends RuntimeException {
    public AiEmptyResponseException() {
        super("AI 응답이 비어있습니다.");
    }
}