package com.example.f_f.email.service;

import com.example.f_f.global.exception.EmailNotVerifiedException;
import com.example.f_f.global.exception.InvalidVerificationCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final StringRedisTemplate redis;
    private final JavaMailSender mailSender;
    private final Random random = new Random();
    // TTL 정책
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(15);


    @Override
    public void sendVerificationCode(String email, String purpose) {
        String code = generate6Digit();

        // Redis에 저장 (5분 유효)
        redis.opsForValue().set(codeKey(email, purpose), code, CODE_TTL);

        // 메일 전송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[FilterFacts] " + purpose + " 이메일 인증 코드");
        message.setText("안녕하세요!\n\n요청하신 인증 코드는 " + code + " 입니다.\n5분 내에 입력해주세요.");
        mailSender.send(message);

        System.out.println("[EMAIL] send code " + code + " to " + email + " for " + purpose);
    }


    @Override
    public void verifyAndMarkAsVerified(String email, String purpose, String code) {
        String saved = redis.opsForValue().get(codeKey(email, purpose));
        if (saved == null || !saved.equals(code)) {
            throw new InvalidVerificationCodeException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }
        // 사용 후 즉시 코드 삭제
        redis.delete(codeKey(email, purpose));
        // 인증 완료 마크(15분 동안 유효)
        redis.opsForValue().set(verifiedKey(email, purpose), "1", VERIFIED_TTL);
    }


    @Override
    public void ensureVerified(String email, String purpose) {
        String v = redis.opsForValue().get(verifiedKey(email, purpose));
        if (v == null) {
            throw new EmailNotVerifiedException(email, "signup");
        }
        // 사용 직후 바로 지워 ‘일회성’으로 만들고 싶으면 아래 주석 해제
         redis.delete(verifiedKey(email, purpose));
    }

    private String codeKey(String email, String purpose) {
        return "ev:code:" + purpose + ":" + email;
    }
    private String verifiedKey(String email, String purpose) {
        return "ev:verified:" + purpose + ":" + email;
    }
    private String generate6Digit() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}