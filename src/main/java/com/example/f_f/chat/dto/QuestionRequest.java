package com.example.f_f.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class QuestionRequest {
    private Long conversationId;
    private String question;
}
