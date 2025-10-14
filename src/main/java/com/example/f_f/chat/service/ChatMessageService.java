//package com.example.f_f.chat.service;
//
//import com.example.f_f.ai.dto.AnswerResponse;
//import com.example.f_f.chat.dto.ChatMessageDto;
//import com.example.f_f.config.Role;
//import com.example.f_f.chat.entity.ChatMessage;
//import com.example.f_f.chat.entity.Conversation;
//import com.example.f_f.chat.repository.ChatMessageRepository;
//import com.example.f_f.chat.repository.ConversationRepository;
//import com.example.f_f.global.exception.CustomException;
//import com.example.f_f.global.exception.RsCode;
//import com.example.f_f.user.repository.UserRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Service
//@RequiredArgsConstructor
//public class ChatMessageService {
//
//    private final ConversationRepository conversationRepo;
//    private final ChatMessageRepository messageRepo;
//    private final UserRepository userRepository;
//
//    // FastAPI 연동용
//    private final WebClient fastApiClient;
//
//    @Value("${fastapi.ask-path:/api/ask}")
//    private String askPath;
//
//
//    /** 사용자 메시지 추가 */
//    @Transactional
//    public ChatMessage addUserMessage(Long conversationId, String userId, String content) {
//        Conversation conv = conversationRepo.findById(conversationId)
//                .orElseThrow(() -> new CustomException(RsCode.CHATROOM_NOT_FOUND));
//
//        // 소유자 검증
//        if (!conv.getUser().getUserId().equals(userId)) {
//            throw new CustomException(RsCode.FORBIDDEN);
//        }
//
//        ChatMessage m = ChatMessage.builder()
//                .conversation(conv)
//                .role(Role.USER)
//                .content(content)
//                .build();
//
//        return messageRepo.save(m);
//    }
//
//
//    /** 어시스턴트 메시지 추가 (→ FastAPI 호출, 응답 저장) */
//    @Transactional
//    public AnswerResponse addAssistantMessage(Long conversationId, String userId, String userQuestion) {
//        // 대화방 존재 검증
//        Conversation conv = conversationRepo.findById(conversationId)
//                .orElseThrow(() -> new CustomException(RsCode.CHATROOM_NOT_FOUND));
//
//        // 대화방/소유자 검증
//        if (!conv.getUser().getUserId().equals(userId)) {
//            throw new CustomException(RsCode.FORBIDDEN);
//        }
//
//        // 2) 사용자 메시지 저장 (ROLE = USER)
//        ChatMessage userMsg = ChatMessage.builder()
//                .conversation(conv)
//                .role(Role.USER)
//                .content(userQuestion)
//                .build();
//        messageRepo.save(userMsg);
//
//        // 3) FastAPI 호출 (실제 구현 시 WebClient 사용)
//        // AnswerResponse aiAnswer = fastApiClient.post()
//        //        .uri(askPath)
//        //        .bodyValue(new UserQuestionDto(userQuestion))
//        //        .retrieve()
//        //        .bodyToMono(AnswerResponse.class)
//        //        .block(Duration.ofSeconds(10));
//
//        AnswerResponse aiAnswer = new AnswerResponse("임시 답변");
//
//        // ai 응답 없음
//        if (aiAnswer == null || aiAnswer.answer() == null || aiAnswer.answer().isBlank()) {
//            throw new CustomException(RsCode.AI_EMPTY_RESPONSE);
//        }
//
//        // 4) 어시스턴트 메시지 저장 (ROLE = ASSISTANT)
//        ChatMessage botMsg = ChatMessage.builder()
//                .conversation(conv)
//                .role(Role.ASSISTANT)
//                .content(aiAnswer.answer()) // ✅ record 필드 접근
//                .build();
//        messageRepo.save(botMsg);
//
//        // 5) 클라이언트로는 AnswerResponse 그대로 반환
//        return aiAnswer;
//    }
//
//
//    /** 메시지 목록(시간순) */
//    public Page<ChatMessageDto> listMessages(String userId, Long conversationId, int page, int size) {
//        Conversation conversation = conversationRepo.findById(conversationId)
//                .orElseThrow(() -> new CustomException(RsCode.CHATROOM_NOT_FOUND));
//
//        // 방 주인인지 검증
//        if (!conversation.getUser().getUserId().equals(userId)) {
//            throw new CustomException(RsCode.FORBIDDEN);
//        }
//
//        return messageRepo.findMessageDtosByConversationId(
//                conversationId, PageRequest.of(page, size));
//    }
//}
