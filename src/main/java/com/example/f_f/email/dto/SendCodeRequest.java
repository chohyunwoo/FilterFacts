package com.example.f_f.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendCodeRequest(
        @Email @NotBlank String email,
        @NotBlank String purpose // "signup" / "reset" 등
) {}