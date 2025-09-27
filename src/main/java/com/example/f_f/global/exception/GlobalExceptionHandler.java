package com.example.f_f.global.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.net.BindException;
import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 커스텀 예외 ----------
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        RsCode rsCode = e.getRsCode();
        log.debug("Custom exception: {}", rsCode.getCode(), e);
        return ResponseEntity.status(rsCode.getStatus()).body(ErrorResponse.of(rsCode));
    }

    // ---------- 요청 검증/바인딩 ----------
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            BindException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        log.debug("Validation/Binding error: {}", e.getMessage(), e);
        return ResponseEntity.status(RsCode.BAD_REQUEST.getStatus())
                .body(ErrorResponse.of(RsCode.BAD_REQUEST));
    }

    // ---------- 인증/인가 ----------
    @ExceptionHandler({ AuthenticationException.class, AccessDeniedException.class })
    public ResponseEntity<ErrorResponse> handleAuth(Exception e) {
        log.debug("Auth/Access error: {}", e.getMessage(), e);
        return ResponseEntity.status(RsCode.UNAUTHORIZED.getStatus())
                .body(ErrorResponse.of(RsCode.UNAUTHORIZED));
    }

    // ---------- 그 외 모든 예외 ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(RsCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(RsCode.INTERNAL_SERVER_ERROR));
    }
}