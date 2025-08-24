package com.example.f_f.ai.controller;

import com.example.f_f.ai.dto.AnswerResponse;
import com.example.f_f.ai.dto.UserQuestionDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatProxyController {

    private final WebClient fastApiClient;

    @Value("${fastapi.ask-path:/api/ask}")
    private String askPath;

    /** 클라이언트 → (스프링) → FastAPI */
    @PostMapping(
            path = "/ask",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<AnswerResponse>> askToFastApi(
            @Valid @RequestBody UserQuestionDto req
    ) {
        return fastApiClient.post()
                .uri(askPath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req) // {"question":"..."} 그대로 전달
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(msg -> new RuntimeException("FastAPI error: " + msg))
                )
                .bodyToMono(AnswerResponse.class)
                .timeout(Duration.ofSeconds(10))
                .map(ResponseEntity::ok);
    }
}