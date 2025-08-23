package com.example.f_f.email.service;

public interface EmailVerificationService {
    void sendVerificationCode(String email, String purpose);
    void verifyAndMarkAsVerified(String email, String purpose, String code);

    /** 회원가입 등 사전에 인증이 끝났는지 확인 (미인증이면 IllegalStateException) */
    void ensureVerified(String email, String purpose);
}