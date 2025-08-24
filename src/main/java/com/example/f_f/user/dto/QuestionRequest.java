package com.example.f_f.user.dto;

import com.example.f_f.config.Category;
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