package com.example.f_f.chat.controller;

import com.example.f_f.chat.dto.AnswerResponse;
import com.example.f_f.chat.dto.ChatMessageDto;
import com.example.f_f.chat.dto.QuestionRequest;
import com.example.f_f.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/messages")
    public ResponseEntity<Page<ChatMessageDto>> listMessages(@RequestParam Long conversationId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatMessageService.listMessages(conversationId, page, size));
    }

    /** 안드로이드: 질문 → (AI)응답 한 번에 */
    @PostMapping("/ask")
    public ResponseEntity<AnswerResponse> ask(Authentication auth, @RequestBody QuestionRequest req) {
//        if (auth.getName() == null || auth.getName().isBlank())
//            throw new IllegalArgumentException("userId is required");
//        if (req.getConversationId() == null)
//            throw new IllegalArgumentException("conversationId is required");
//        if (req.getQuestion() == null || req.getQuestion().isBlank())
//            throw new IllegalArgumentException("question is required");

        // 3) 어시스턴트 답변 저장
        AnswerResponse answer = chatMessageService.addAssistantMessage(req.getConversationId(), auth.getName(), req.getQuestion());

        // 4) 응답
        return ResponseEntity.ok(answer);
    }
}