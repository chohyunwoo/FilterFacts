package com.example.f_f.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestionRequest {
    @NotBlank
    private String question;
    private String userId;
    private Category category;
}