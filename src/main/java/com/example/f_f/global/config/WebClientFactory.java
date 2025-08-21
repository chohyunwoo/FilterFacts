package com.example.f_f.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Component // 스프링 컴포넌트로 등록
@RequiredArgsConstructor // final 필드를 자동으로 생성자 주입
public class WebClientFactory {

    private final ApiProperties apiProperties; // application.yml에서 로드한 API 설정 정보를 주입받음

    public WebClient create(String apiName) {
        // API 이름(mfds 등)에 해당하는 설정 정보 가져오기
        ApiProperties.ApiConfig config = apiProperties.getApis().get(apiName);

        // 설정이 없을 경우 예외 발생
        if (config == null) {
            throw new IllegalArgumentException("해당 API 설정이 존재하지 않습니다: " + apiName);
        }

        System.out.println("🌐 WebClient 생성 - API 이름: " + apiName + ", baseUrl: " + config.getBaseUrl());

        // baseUrl을 기반으로 WebClient 생성하여 반환
        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .exchangeStrategies(ExchangeStrategies.builder()    // 기본 maxInMemorySize(256KB) 초과로 인한 예외 방지용 - 대용량 응답 대비 10MB로 설정
                        .codecs(clientCodecConfigurer ->
                                clientCodecConfigurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                        .build())
                .build();
    }
}
