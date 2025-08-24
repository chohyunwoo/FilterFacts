package com.example.f_f.chat.service;

import com.example.f_f.chat.dto.AnswerResponse;
import com.example.f_f.chat.dto.ChatMessageDto;
import com.example.f_f.chat.dto.Role;
import com.example.f_f.chat.entity.ChatMessage;
import com.example.f_f.chat.entity.Conversation;
import com.example.f_f.chat.repository.ChatMessageRepository;
import com.example.f_f.chat.repository.ConversationRepository;
import com.example.f_f.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ConversationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;
    private final UserRepository userRepository;


    /** 사용자 메시지 추가 */
    @Transactional
    public ChatMessage addUserMessage(Long conversationId, String userId, String content) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대화방"));

        // 소유자 검증
        if (!conv.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("대화방 소유자가 아님");
        }

        ChatMessage m = ChatMessage.builder()
                .conversation(conv)
                .role(Role.USER)
                .content(content)
                .build();

        return messageRepo.save(m);
    }

    /** 어시스턴트 메시지 추가 */
    @Transactional
    public AnswerResponse addAssistantMessage(Long conversationId, String userId, String content) {
        // 대화방 존재 확인
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대화방"));

        // 대화방 소유자인지 확인
        if (!conv.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("대화방 소유자가 아님");
        }

        // 채팅 메시지 저장
        ChatMessage m = ChatMessage.builder()
                .conversation(conv)
                .role(Role.ASSISTANT)
                .content(content)
                .build();

        // ai server로 요청 넘기기
        // apiService.sendRequest(content);

        messageRepo.save(m);
        return new AnswerResponse("ai 응답");
    }


    /** 메시지 목록(시간순) */
    public Page<ChatMessageDto> listMessages(Long conversationId, int page, int size) {
        return messageRepo.findMessageDtosByConversationId(
                conversationId, PageRequest.of(page, size));
    }
}
