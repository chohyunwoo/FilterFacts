package com.example.f_f.chat.controller;

import com.example.f_f.chat.dto.*;
import com.example.f_f.chat.entity.ChatMessage;
import com.example.f_f.chat.entity.Conversation;
import com.example.f_f.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatService;

    @PostMapping("/conversations")
    public StartConversationResponse startConversation(@RequestBody StartConversationRequest req) {
        Conversation c = chatService.startConversation(req.getUserId(), req.getTitle());
        return new StartConversationResponse(c.getId());
    }

    @GetMapping("/conversations")
    public Page<Conversation> listConversations(@RequestParam String userId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return chatService.listConversations(userId, page, size);
    }

    @PostMapping("/messages")
    public SendMessageResponse sendMessage(@RequestBody SendMessageRequest req) {
        ChatMessage m = ("assistant".equalsIgnoreCase(req.getRole()))
                ? chatService.addAssistantMessage(req.getConversationId(), req.getUserId(), req.getContent())
                : chatService.addUserMessage(req.getConversationId(), req.getUserId(), req.getContent(), req.getKeywords());
        return new SendMessageResponse(m.getId());
    }

    @GetMapping("/messages")
    public Page<ChatMessage> listMessages(@RequestParam Long conversationId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "50") int size) {
        return chatService.listMessages(conversationId, page, size);
    }

    // ✅ 안드로이드가 호출하는 엔드포인트 (질문→답변 한 번에)
    @PostMapping("/ask")
    public AnswerResponse ask(@RequestBody QuestionRequest req) {
        if (req.getUserId() == null || req.getUserId().isBlank())
            throw new IllegalArgumentException("userId is required");
        if (req.getConversationId() == null)
            throw new IllegalArgumentException("conversationId is required");
        if (req.getQuestion() == null || req.getQuestion().isBlank())
            throw new IllegalArgumentException("question is required");

        // (선택) 키워드 추출이 없으면 빈 배열
        List<String> keywords = req.getKeywords() != null ? req.getKeywords() : Collections.emptyList();

        // 1) 사용자 메시지 저장
        chatService.addUserMessage(req.getConversationId(), req.getUserId(), req.getQuestion(), keywords);

        // 2) 답변 생성
        //    아직 AI 연동이 없다면 모의 응답으로 동작 확인 가능
        String answer = "모의응답: \"" + req.getQuestion() + "\"";

        //    실제 AI가 있다면 여기서 호출하세요:
        //    String answer = aiClient.ask(req.getQuestion(), keywords);

        // 3) 어시스턴트 메시지 저장
        chatService.addAssistantMessage(req.getConversationId(), req.getUserId(), answer);

        // 4) 안드로이드로 반환
        return new AnswerResponse(answer, keywords);
    }
}
