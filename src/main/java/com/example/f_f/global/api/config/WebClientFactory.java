package com.example.f_f.global.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class WebClientFactory {

    private final ApiProperties apiProperties;

    public WebClient create(String apiName) {
        ApiProperties.ApiConfig config = apiProperties.getApis().get(apiName);
        if (config == null) {
            throw new IllegalArgumentException("해당 API 설정이 존재하지 않습니다: " + apiName);
        }

        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();
    }
}