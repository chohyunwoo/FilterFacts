package com.example.f_f.chat.entity;

import com.example.f_f.config.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name="chat_messages")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="conversation_id")
    private Conversation conversation;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @Column(nullable=false, length=20)
    private Role role; // "USER", "ASSISTANT" 등

    @Column(columnDefinition = "text")
    private String content;

    @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
}