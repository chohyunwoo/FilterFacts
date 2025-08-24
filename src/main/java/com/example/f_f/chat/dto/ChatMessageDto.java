package com.example.f_f.chat.dto;

import java.time.Instant;

public record ChatMessageDto(
        Role role,
        Instant createdAt,
        String content
) {}