package com.example.f_f.chat.service;

import com.example.f_f.chat.dto.AnswerResponse;
import com.example.f_f.chat.dto.ChatSaver;
import com.example.f_f.chat.dto.QuestionRequest;
import com.example.f_f.chat.keyword.KeywordExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatAppService {

    private final KeywordExtractor keywordExtractor;
    private final AiClient ai;           // 기존 그대로 사용
    private final ChatSaver chatSaver;   // ✅ 새로 주입

    @Value("${app.mock:false}")
    private boolean mockMode;

    public Mono<AnswerResponse> ask(QuestionRequest req) {
        final long start = System.currentTimeMillis();

        // 필수 파라미터 확인
        if (req.getUserId() == null || req.getUserId().isBlank())
            return Mono.error(new IllegalArgumentException("userId 필수"));
        if (req.getConversationId() == null || req.getConversationId() <= 0)
            return Mono.error(new IllegalArgumentException("conversationId 필수"));
        if (req.getQuestion() == null || req.getQuestion().isBlank())
            return Mono.error(new IllegalArgumentException("question 필수"));

        final String userId = req.getUserId().trim();
        final Long conversationId = req.getConversationId();
        final List<String> keywords = keywordExtractor.extract(req.getQuestion());

        if (mockMode) {
            String answer = "✅ 모의응답: \"" + req.getQuestion() + "\"\n키워드: " + String.join(", ", keywords);

            // JPA 저장은 blocking → 별도 스케줄러에서
            Schedulers.boundedElastic().schedule(() ->
                    chatSaver.saveExchange(conversationId, userId, req.getQuestion(), keywords, answer)
            );

            return Mono.just(new AnswerResponse(answer, keywords));
        }

        // 실제 AI 호출
        return ai.ask(req.getQuestion(), keywords)                 // Mono<String>
                .publishOn(Schedulers.boundedElastic())           // 이후 저장은 blocking
                .doOnNext(answer ->
                        chatSaver.saveExchange(conversationId, userId, req.getQuestion(), keywords, answer)
                )
                .map(answer -> new AnswerResponse(answer, keywords));
    }
}