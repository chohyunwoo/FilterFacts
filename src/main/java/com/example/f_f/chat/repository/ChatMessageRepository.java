package com.example.f_f.chat.repository;

import com.example.f_f.chat.dto.ChatMessageDto;
import com.example.f_f.chat.entity.ChatMessage;
import com.example.f_f.chat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("""
           SELECT new com.example.f_f.chat.dto.ChatMessageDto(m.role, m.createdAt, m.content)
           FROM ChatMessage m
           WHERE m.conversation.id = :conversationId
           ORDER BY m.id ASC
           """)
    Page<ChatMessageDto> findMessageDtosByConversationId(Long conversationId, Pageable pageable);
}
