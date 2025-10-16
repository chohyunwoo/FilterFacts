package com.example.f_f.global.exception;

public record SimpleErrorResponse(String message) {
    public static SimpleErrorResponse of(String message) {
        return new SimpleErrorResponse(message);
    }
}