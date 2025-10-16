package com.example.f_f.email.controller;

import com.example.f_f.email.dto.SendCodeRequest;
import com.example.f_f.email.dto.VerifyCodeRequest;
import com.example.f_f.email.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailVerificationController {

    private final EmailVerificationService service;

    public EmailVerificationController(EmailVerificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> send(@RequestBody @Valid SendCodeRequest req) {
        service.sendVerificationCode(req.email(), req.purpose());
        return ResponseEntity.ok("인증 코드가 전송되었습니다.");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody @Valid VerifyCodeRequest req) {
        service.verifyAndMarkAsVerified(req.email(), req.purpose(), req.code());
        return ResponseEntity.ok("인증 코드가 확인되었습니다.");
    }
}