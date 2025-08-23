package com.example.f_f.chat.repository;

import com.example.f_f.chat.entity.ChatMessage;
import com.example.f_f.chat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByConversationOrderByIdDesc(Conversation conversation, Pageable pageable);
    Page<ChatMessage> findByConversationIdOrderByIdDesc(Long conversationId, Pageable pageable);
}
