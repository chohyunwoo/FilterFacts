package com.example.f_f.chat.dto;


import com.example.f_f.user.dto.Category;
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
    private String content;
    private Category category;
}