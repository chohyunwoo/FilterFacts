package com.example.f_f.chat.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    private Long conversationId;
    private String userId;
    private String role;             // "user" | "assistant" (보통 클라에서는 "user")
    private String content;
    private List<String> keywords;   // 선택
}