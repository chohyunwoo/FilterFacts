//package com.example.f_f.chat.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class AiClient {
//
//    private final WebClient webClient;
//    private final String path;
//
//    public AiClient(@Value("${ai.base-url}") String baseUrl,
//                    @Value("${ai.path:/infer}") String path) {
//        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
//        this.path = path;
//    }
//
//    public Mono<String> ask(String question, List<String> keywords) {
//        Map<String, Object> payload = Map.of(
//                "question", question,
//                "keywords", keywords
//        );
//        return webClient.post()
//                .uri(path)
//                .bodyValue(payload)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .map(body -> (String) body.getOrDefault("answer", ""));
//    }
//}
