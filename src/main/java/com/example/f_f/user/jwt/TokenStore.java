package com.example.f_f.user.jwt;

public interface TokenStore {
    void saveRefresh(String username, String refreshToken, long ttlMs);
    boolean isRefreshValid(String username, String refreshToken);
    void revokeRefresh(String username, String refreshToken);
    void revokeAll(String username);    // 모든 기기 로그아웃
}