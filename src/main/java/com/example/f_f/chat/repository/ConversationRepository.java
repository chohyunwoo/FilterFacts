package com.example.f_f.chat.repository;

import com.example.f_f.chat.dto.StartConversationResponse;
import com.example.f_f.chat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT new com.example.f_f.chat.dto.StartConversationResponse(c.id, c.title, c.createdAt) " +
            "FROM Conversation c " +
            "WHERE c.user.userId = :userId " +
            "ORDER BY c.createdAt DESC")
    Page<StartConversationResponse> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
