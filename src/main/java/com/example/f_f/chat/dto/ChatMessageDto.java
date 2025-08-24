package com.example.f_f.chat.dto;

import com.example.f_f.config.Role;

import java.time.Instant;

public record ChatMessageDto(
        Role role,
        Instant createdAt,
        String content
) {}