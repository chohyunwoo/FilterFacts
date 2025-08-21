package com.example.f_f.global.config;

// Lombok을 사용해 getter/setter 자동 생성
import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component // 스프링이 이 클래스를 빈으로 자동 등록
@ConfigurationProperties(prefix = "external-api") // application.yml의 external-api 설정을 주입받음
@Getter
@Setter
public class ApiProperties {

    // 설정된 외부 API들의 이름-설정 매핑
    private Map<String, ApiConfig> apis = new HashMap<>();

    // 외부 API 하나의 설정값을 나타내는 내부 클래스
    @Getter @Setter
    public static class ApiConfig {
        private String baseUrl; // ex) https://api.odcloud.kr/openapi
    }
}
