package com.example.f_f.user.service;

import com.example.f_f.email.service.EmailVerificationService;
import com.example.f_f.global.exception.DuplicateUserIdException;
import com.example.f_f.global.exception.InvalidRefreshTokenException;
import com.example.f_f.user.dto.*;
import com.example.f_f.user.entity.User;
import com.example.f_f.user.jwt.JwtService;
import com.example.f_f.user.jwt.TokenStore;
import com.example.f_f.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final TokenStore store;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    // 회원가입
    public String register(RegisterRequest req) {
        // userId 중복 확인
        if (users.existsByUserId(req.userId())) {
            throw new DuplicateUserIdException(req.userId());
        }

        // 이메일 인증 선확인
        emailVerificationService.ensureVerified(req.email(), "signup");

        User u = new User();
        u.setUserId(req.userId());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setEmail(req.email());
        users.save(u);
        return u.getUserId();
    }

    // 로그인
    public TokenResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        UserDetails user = (UserDetails) auth.getPrincipal();

        String access = jwt.generateAccessToken(user);
        String refresh = jwt.generateRefreshToken(user);

        long expMs = jwt.getExpiration(access).getTime() - System.currentTimeMillis();
        long refreshTtl = jwt.getExpiration(refresh).getTime() - System.currentTimeMillis();
        store.saveRefresh(user.getUsername(), refresh, refreshTtl);

        return new TokenResponse(access, expMs, refresh);
    }

    // 토큰 재발급
    public TokenResponse refresh(RefreshRequest req) {
        String refresh = req.refreshToken();
        if (!jwt.isTokenValid(refresh) || !jwt.isRefresh(refresh)) {
            throw new InvalidRefreshTokenException();
        }

        String username = jwt.getUsername(refresh);
        if (!store.isRefreshValid(username, refresh)) {
            throw new InvalidRefreshTokenException();
        }

        store.revokeRefresh(username, refresh);

        UserDetails stub = org.springframework.security.core.userdetails.User
                .withUsername(username).password("N/A").build();

        String newAccess = jwt.generateAccessToken(stub);
        String newRefresh = jwt.generateRefreshToken(stub);

        long expMs = jwt.getExpiration(newAccess).getTime() - System.currentTimeMillis();
        long refreshTtl = jwt.getExpiration(newRefresh).getTime() - System.currentTimeMillis();
        store.saveRefresh(username, newRefresh, refreshTtl);

        return new TokenResponse(newAccess, expMs, newRefresh);
    }

    public void logout(LogoutRequest req) {
        String refresh = req.refreshToken();
        if (!jwt.isTokenValid(refresh) || !jwt.isRefresh(refresh)) {
            throw new InvalidRefreshTokenException();
        }
        String username = jwt.getUsername(refresh);
        store.revokeRefresh(username, refresh);
    }

    public void logoutAll(String username) {
        store.revokeAll(username);
    }
}
