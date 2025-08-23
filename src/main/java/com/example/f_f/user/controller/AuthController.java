package com.example.f_f.user.controller;

import com.example.f_f.email.service.EmailVerificationService;
import com.example.f_f.user.dto.*;
import com.example.f_f.user.entity.User;
import com.example.f_f.user.jwt.JwtService;
import com.example.f_f.user.jwt.TokenStore;
import com.example.f_f.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final TokenStore store;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;



    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        System.out.println("req.userId() = " + req.userId());
        System.out.println("req.email() = " + req.email());
        System.out.println("req.password() = " + req.password());
        if (users.existsByUserId(req.userId())) {
            return ResponseEntity.status(409).body("userId already exists");
        }

        // ✅ 회원가입 전에 이메일 인증 완료 여부 확인 (purpose="signup")
        emailVerificationService.ensureVerified(req.email(), "signup");

        User u = new User();
        u.setUserId(req.userId());
        u.setPassword(passwordEncoder.encode(req.password()));
        // 필요시 User 엔티티에 email 필드 추가
        u.setEmail(req.email());
        users.save(u);

        return ResponseEntity.created(java.net.URI.create("/api/users/" + u.getUserId())).build();
    }



    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        UserDetails user = (UserDetails) auth.getPrincipal();

        String access = jwt.generateAccessToken(user);
        String refresh = jwt.generateRefreshToken(user);

        long expMs = jwt.getExpiration(access).getTime() - System.currentTimeMillis();
        long refreshTtl = jwt.getExpiration(refresh).getTime() - System.currentTimeMillis();
        store.saveRefresh(user.getUsername(), refresh, refreshTtl);

        return ResponseEntity.ok(new TokenResponse(access, expMs, refresh));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        String refresh = req.refreshToken();
        if (!jwt.isTokenValid(refresh) || !jwt.isRefresh(refresh)) return ResponseEntity.status(401).build();

        String username = jwt.getUsername(refresh);
        if (!store.isRefreshValid(username, refresh)) return ResponseEntity.status(401).build();

        store.revokeRefresh(username, refresh);

        UserDetails stub = org.springframework.security.core.userdetails.User
                .withUsername(username).password("N/A").build(); // 권한 없음

        String newAccess = jwt.generateAccessToken(stub);
        String newRefresh = jwt.generateRefreshToken(stub);

        long expMs = jwt.getExpiration(newAccess).getTime() - System.currentTimeMillis();
        long refreshTtl = jwt.getExpiration(newRefresh).getTime() - System.currentTimeMillis();
        store.saveRefresh(username, newRefresh, refreshTtl);

        return ResponseEntity.ok(new TokenResponse(newAccess, expMs, newRefresh));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() { return ResponseEntity.ok().build(); }

    // AuthController 에 추가
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest req) {
        String refresh = req.refreshToken();
        if (!jwt.isTokenValid(refresh) || !jwt.isRefresh(refresh)) {
            return ResponseEntity.status(400).build(); // 형식/타입 아님
        }
        String username = jwt.getUsername(refresh);
        store.revokeRefresh(username, refresh);
        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@RequestBody(required = false) String ignore) {
        String username = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal(); // 또는 getName()
        store.revokeAll(username);
        return ResponseEntity.noContent().build();
    }
}