package com.example.f_f.chat.repository;

import com.example.f_f.chat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConverSationRepository  extends JpaRepository<Conversation, Long> {
    Page<Conversation> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}