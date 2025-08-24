package com.example.f_f.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GrobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        // 서비스에서 throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}