package com.example.f_f.global.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "external-api")
@Getter
@Setter
public class ApiProperties {

    private Map<String, ApiConfig> apis = new HashMap<>();

    @Getter @Setter
    public static class ApiConfig {
        private String baseUrl;
    }
}
