package com.example.f_f.ai.dto;

/** FastAPI → 스프링으로 돌아오는 응답 DTO */
public record AnswerResponse(
        String answer
) {}