//package com.example.f_f.chat.service;
//
//import com.example.f_f.chat.dto.ChatSaver;
//import com.example.f_f.chat.entity.ChatMessage;
//import com.example.f_f.chat.entity.Conversation;
//import com.example.f_f.chat.repository.ChatMessageRepository;
//import com.example.f_f.chat.repository.ConversationRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class ChatSaverJpa implements ChatSaver {
//
//    private final ConversationRepository conversationRepo;
//    private final ChatMessageRepository messageRepo;
//
//    @Transactional
//    @Override
//    public void saveExchange(long conversationId,
//                             String userId,
//                             String question,
//                             List<String> keywords,
//                             String answer) {
//        Conversation conv = conversationRepo.findById(conversationId)
//                .orElseThrow(() -> new IllegalArgumentException("없는 대화방: " + conversationId));
//
//        // 1) 사용자 메시지
//        ChatMessage userMsg = ChatMessage.builder()
//                .conversation(conv)
//                .userId(userId)
//                .role("user")
//                .content(question)
//                .keywords(keywords)
//                .build();
//        messageRepo.save(userMsg);
//
//        // 2) 어시스턴트(답변) 메시지
//        ChatMessage botMsg = ChatMessage.builder()
//                .conversation(conv)
//                .userId(userId) // 필요 시 시스템 userId 별도 사용 가능
//                .role("assistant")
//                .content(answer)
//                .build();
//        messageRepo.save(botMsg);
//    }
//}