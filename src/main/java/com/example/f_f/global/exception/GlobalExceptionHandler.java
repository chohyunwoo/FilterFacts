package com.example.f_f.global.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 공통: 요청 검증/바인딩 ----------
    @ExceptionHandler({
            MethodArgumentNotValidException.class, // @Valid on @RequestBody
            ConstraintViolationException.class,    // @Validated on @RequestParam/@PathVar
            BindException.class,                   // 바인딩 에러
            HttpMessageNotReadableException.class  // JSON 파싱 실패
    })
    public ResponseEntity<SimpleErrorResponse> handleBadRequest(Exception e) {
        log.debug("Validation/Binding error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(SimpleErrorResponse.of("요청 값이 올바르지 않습니다."));
    }

    // ---------- 인증/인가 ----------
    // 팀 정책: 인증되지 않은 접근도 403으로 통일
    @ExceptionHandler({ AuthenticationException.class, AccessDeniedException.class })
    public ResponseEntity<SimpleErrorResponse> handleAuth(Exception e) {
        log.debug("Auth/Access error: {}", e.getMessage(), e);
        return ResponseEntity.status(403).body(SimpleErrorResponse.of("인증이 필요합니다."));
    }

    // ---------- 사용자/리소스 ----------
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleUserNotFound(UserNotFoundException e) {
        log.debug("User not found: {}", e.getMessage(), e);
        return ResponseEntity.status(404).body(SimpleErrorResponse.of("요청한 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleConversationNotFound(ConversationNotFoundException e) {
        log.debug("Conversation not found: {}", e.getMessage(), e);
        return ResponseEntity.status(404).body(SimpleErrorResponse.of("존재하지 않는 채팅방입니다."));
    }

    @ExceptionHandler(ConversationForbiddenException.class)
    public ResponseEntity<SimpleErrorResponse> handleConversationForbidden(ConversationForbiddenException e) {
        log.debug("Conversation forbidden: {}", e.getMessage(), e);
        return ResponseEntity.status(403).body(SimpleErrorResponse.of("접근 권한이 없습니다."));
    }

    // ---------- Auth 도메인 ----------
    @ExceptionHandler(DuplicateUserIdException.class)
    public ResponseEntity<SimpleErrorResponse> handleDuplicateUserId(DuplicateUserIdException e) {
        log.debug("Duplicate userId: {}", e.getMessage(), e);
        return ResponseEntity.status(409).body(SimpleErrorResponse.of("이미 사용 중인 아이디입니다."));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<SimpleErrorResponse> handleEmailNotVerified(EmailNotVerifiedException e) {
        log.debug("Email not verified: {}", e.getMessage(), e);
        return ResponseEntity.status(412).body(SimpleErrorResponse.of("이메일 인증이 필요합니다."));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<SimpleErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        log.debug("Invalid refresh token: {}", e.getMessage(), e);
        return ResponseEntity.status(401).body(SimpleErrorResponse.of("유효하지 않은 리프레시 토큰입니다."));
    }

    // ---------- Email 도메인 ----------
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<SimpleErrorResponse> handleInvalidVerificationCode(InvalidVerificationCodeException e) {
        log.debug("Invalid verification code: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(SimpleErrorResponse.of("인증 코드가 올바르지 않거나 만료되었습니다."));
    }

    // ---------- AI 연동 ----------
    @ExceptionHandler(AiUpstreamException.class)
    public ResponseEntity<SimpleErrorResponse> handleAiUpstream(AiUpstreamException e) {
        log.warn("AI upstream error: status={}, body={}", e.getStatus(), truncate(e.getResponseBody()));
        return ResponseEntity.status(502).body(SimpleErrorResponse.of("외부 서비스 오류가 발생했습니다."));
    }

    @ExceptionHandler(AiTimeoutException.class)
    public ResponseEntity<SimpleErrorResponse> handleAiTimeout(AiTimeoutException e) {
        log.warn("AI timeout: {}", e.getMessage());
        return ResponseEntity.status(504).body(SimpleErrorResponse.of("외부 서비스 응답이 지연되었습니다."));
    }

    @ExceptionHandler(AiEmptyResponseException.class)
    public ResponseEntity<SimpleErrorResponse> handleAiEmpty(AiEmptyResponseException e) {
        log.warn("AI empty response: {}", e.getMessage());
        return ResponseEntity.status(502).body(SimpleErrorResponse.of("외부 서비스 오류가 발생했습니다."));
    }

    // ---------- 그외 오류 ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleAll(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(500).body(SimpleErrorResponse.of("서버 내부 오류가 발생했습니다."));
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
}