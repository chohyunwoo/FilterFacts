package com.example.f_f.user.dto;

public record TokenResponse(String accessToken, long expiresInMs, String refreshToken) {}