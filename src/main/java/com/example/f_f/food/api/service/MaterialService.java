package com.example.f_f.food.api.service;

import com.example.f_f.food.api.dto.MaterialDto;
import com.example.f_f.food.api.dto.ReportDto;
import com.example.f_f.food.api.repository.MaterialRepository;
import com.example.f_f.food.api.repository.ReportRepository;
import com.example.f_f.global.config.WebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final WebClientFactory webClientFactory;
    private final MaterialRepository materialRepository;

    @PostConstruct
    public void init() {
        fetchAndSave();
    }

    @Transactional
    public void fetchAndSave() {
        WebClient client = webClientFactory.create("material");

        try {
            String response = client.get()
                    .uri(b -> b.pathSegment(
                            "2da40e9812a643ebbdc6",  // TODO: 환경변수/설정 분리 권장
                            "C003",                 // TODO: 실제 서비스ID 확인 필요
                            "json",
                            "1",
                            "100"
                    ).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank() || response.trim().startsWith("<")) {
                return; // API 오류 시 중단
            }

            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(response);

            String serviceId = root.fieldNames().hasNext() ? root.fieldNames().next() : null;
            if (serviceId == null) return;

            JsonNode rows = root.path(serviceId).path("row");
            if (!rows.isArray()) return;

            for (JsonNode item : rows) {
                try {
                    MaterialDto dto = om.treeToValue(item, MaterialDto.class);
                    materialRepository.save(dto.toEntity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}