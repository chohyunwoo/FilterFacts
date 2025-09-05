package com.example.f_f.global.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId) {
        super("존재하지 않는 사용자: " + userId);
    }
}