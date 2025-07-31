package com.example.f_f.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.f_f.user.dto.UserJoinRequest;
import com.example.f_f.user.dto.UserLoginRequest;
import com.example.f_f.user.dto.UserLoginResponse;
import com.example.f_f.user.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping
    public ResponseEntity<Void> register(@RequestBody @Valid UserJoinRequest request) {
        userService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        System.out.println("✅ 로그인 요청이 도착했습니다: " + request);
        return ResponseEntity.ok(userService.login(request));
    }

}
