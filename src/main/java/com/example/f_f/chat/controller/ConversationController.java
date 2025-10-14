//package com.example.f_f.chat.controller;
//
//import com.example.f_f.chat.dto.StartConversationRequest;
//import com.example.f_f.chat.dto.StartConversationResponse;
//import com.example.f_f.chat.entity.Conversation;
//import com.example.f_f.chat.service.ConversationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/chat/conversations")
//@RequiredArgsConstructor
//public class ConversationController {
//
//    private final ConversationService conversationService;
//
//    @PostMapping
//    public ResponseEntity<StartConversationResponse> startConversation(Authentication auth,
//                                                                       @RequestBody StartConversationRequest req) {
//        Conversation conv = conversationService.startConversation(auth.getName(), req.getTitle());
//        return ResponseEntity.ok(new StartConversationResponse(conv.getId(), conv.getTitle(), conv.getCreatedAt()));
//    }
//
//    @GetMapping
//    public ResponseEntity<Page<StartConversationResponse>> listConversations(Authentication auth,
//                                                @RequestParam(defaultValue = "0") int page,
//                                                @RequestParam(defaultValue = "20") int size) {
//        return ResponseEntity.ok(conversationService.listConversations(auth.getName(), page, size));
//    }
//}