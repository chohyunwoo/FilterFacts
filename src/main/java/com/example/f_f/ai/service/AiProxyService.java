//package com.example.f_f.ai.service;
//
//import com.example.f_f.ai.dto.AnswerResponse;
//import com.example.f_f.ai.dto.UserQuestionDto;
//import com.example.f_f.global.exception.CustomException;
//import com.example.f_f.global.exception.RsCode;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//import java.util.concurrent.TimeoutException;
//
//@Service
//@RequiredArgsConstructor
//public class AiProxyService {
//
//    private final WebClient fastApiClient;
//
//    @Value("${fastapi.ask-path:/api/ask}")
//    private String askPath;
//
//    public Mono<AnswerResponse> ask(UserQuestionDto req) {
//        return fastApiClient.post()
//                .uri(askPath)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .bodyValue(req)
//                .retrieve()
//                .onStatus(
//                        status -> status.is4xxClientError() || status.is5xxServerError(),
//                        resp -> resp.bodyToMono(String.class)
//                                .defaultIfEmpty("")
//                                .flatMap(body -> Mono.error(new CustomException(RsCode.AI_UPSTREAM_ERROR)))
//                )
//                .bodyToMono(AnswerResponse.class)
//                .timeout(Duration.ofSeconds(10))
//                .onErrorMap(TimeoutException.class, ex -> new CustomException(RsCode.AI_TIMEOUT))
//                .switchIfEmpty(Mono.error(new CustomException(RsCode.AI_EMPTY_RESPONSE)))
//                .flatMap(ans ->
//                        (ans == null || ans.answer() == null || ans.answer().isBlank())
//                                ? Mono.error(new CustomException(RsCode.AI_EMPTY_RESPONSE))
//                                : Mono.just(ans)
//                );
//    }
//}
