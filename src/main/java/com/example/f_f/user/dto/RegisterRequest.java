package com.example.f_f.user.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String userId,
        @NotBlank @Size(min = 6, max = 100) String password,
        @Email @NotBlank String email
) {}