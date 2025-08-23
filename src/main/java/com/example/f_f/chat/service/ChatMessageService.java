package com.example.f_f.chat.service;

import com.example.f_f.chat.entity.ChatMessage;
import com.example.f_f.chat.entity.Conversation;
import com.example.f_f.chat.repository.ChatMessageRepository;
import com.example.f_f.chat.repository.ConverSationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class ChatMessageService {

    private final ConverSationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;

    @Transactional
    public Conversation startConversation(String userId, String title) {
        Conversation c = Conversation.builder()
                .userId(userId)
                .title(title == null || title.isBlank() ? "새 대화" : title)
                .build();
        return conversationRepo.save(c);
    }

    @Transactional
    public ChatMessage addUserMessage(Long conversationId, String userId, String content, List<String> keywords) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대화방"));
        ChatMessage m = ChatMessage.builder()
                .conversation(conv)
                .userId(userId)
                .role("user")
                .content(content)
                .keywords(keywords)
                .build();
        return messageRepo.save(m);
    }

    @Transactional
    public ChatMessage addAssistantMessage(Long conversationId, String userId, String content) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대화방"));
        ChatMessage m = ChatMessage.builder()
                .conversation(conv)
                .userId(userId)
                .role("assistant")
                .content(content)
                .build();
        return messageRepo.save(m);
    }

    public Page<Conversation> listConversations(String userId, int page, int size) {
        return conversationRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public Page<ChatMessage> listMessages(Long conversationId, int page, int size) {
        return messageRepo.findByConversationIdOrderByIdDesc(conversationId, PageRequest.of(page, size));
    }
}
