package com.example.f_f.chat.service;

import com.example.f_f.chat.dto.StartConversationResponse;
import com.example.f_f.chat.entity.Conversation;
import com.example.f_f.chat.repository.ConversationRepository;
import com.example.f_f.user.entity.User;
import com.example.f_f.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /** 채팅방 생성 */
    @Transactional
    public Conversation startConversation(String userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        Conversation c = Conversation.builder()
                .user(user)
                .title((title == null || title.isBlank()) ? "새 대화" : title)
                .build();
        return conversationRepository.save(c);
    }

    /** 내 채팅방 목록 */
    public Page<StartConversationResponse> listConversations(String userId, int page, int size) {
        return conversationRepository.findByUser_UserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size));
    }

    /** 소유 검증 포함 조회(메시지 서비스에서 사용) */
    public Conversation getOwnedConversation(String userId, Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대화방"));
        if (!conv.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("대화방 소유자가 아님");
        }
        return conv;
    }

    // 이후: 제목 변경, 삭제 등도 여기서 제공 (ex. rename, delete 등)
}