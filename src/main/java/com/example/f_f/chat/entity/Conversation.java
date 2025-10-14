//package com.example.f_f.chat.entity;
//
//import com.example.f_f.user.entity.User;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "conversations")
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Conversation {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // 다:1 (여러 대화가 한 명의 사용자에게)
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    private String title;
//
//    @Column(nullable=false, updatable=false)
//    private Instant createdAt;
//
//    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
//    @OrderBy("createdAt ASC")
//    private List<ChatMessage> messages = new ArrayList<>();
//
//    @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
//}