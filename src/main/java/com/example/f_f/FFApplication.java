package com.example.f_f;

import com.example.f_f.global.api.config.ApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApiProperties.class)
public class FFApplication {

    public static void main(String[] args) {
        SpringApplication.run(FFApplication.class, args);
    }

}
