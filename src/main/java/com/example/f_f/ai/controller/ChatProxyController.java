//package com.example.f_f.ai.controller;
//
//import com.example.f_f.ai.dto.AnswerResponse;
//import com.example.f_f.ai.dto.UserQuestionDto;
//import com.example.f_f.ai.service.AiProxyService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//
//@RestController
//@RequestMapping("/api/ai")
//@RequiredArgsConstructor
//public class ChatProxyController {
//
//    private final AiProxyService aiProxyService;
//
//    @PostMapping(
//            path = "/ask",
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public Mono<ResponseEntity<AnswerResponse>> askToFastApi(
//            @Valid @RequestBody UserQuestionDto req
//    ) {
//        return aiProxyService.ask(req)
//                .map(ResponseEntity::ok);
//    }
//}
