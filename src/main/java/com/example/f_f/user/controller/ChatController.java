package com.example.f_f.user.controller;

import com.example.f_f.user.dto.AnswerResponse;
import com.example.f_f.user.dto.QuestionRequest;
import com.example.f_f.user.service.AiClient;
import com.example.f_f.user.service.keyword.KeywordExtractor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final KeywordExtractor extractor;
    private final AiClient ai;

    @Value("${ai.mock:true}")   // 기본값 true로 두면 AI 없어도 바로 동작
    private boolean mockMode;

    public ChatController(KeywordExtractor extractor, AiClient ai) {
        this.extractor = extractor;
        this.ai = ai;
    }

    @PostMapping("/ask")
    public Mono<AnswerResponse> ask(@Valid @RequestBody QuestionRequest req) {
        System.out.println("req = " + req);

        long start = System.currentTimeMillis();
        List<String> keywords = extractor.extract(req.getQuestion(), 8);

        if (mockMode) {
            // ✅ AI 서버 없이도 즉시 응답
            String dummy = "🧪 모의응답: \"" + req.getQuestion() + "\"\n"
                    + "키워드: " + String.join(", ", keywords);
            return Mono.just(new AnswerResponse(dummy, keywords, "mock-ai", System.currentTimeMillis() - start));
        }

        return ai.ask(req.getQuestion(), keywords)
                .map(answer -> new AnswerResponse(
                        answer, keywords, "my-ai-model", System.currentTimeMillis() - start
                ));
    }
}