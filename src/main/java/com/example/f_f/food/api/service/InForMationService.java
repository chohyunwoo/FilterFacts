package com.example.f_f.food.api.service;

import com.example.f_f.global.config.WebClientFactory;
import com.example.f_f.food.api.dto.InForMationDto;
import com.example.f_f.food.api.repository.InForMationRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class InForMationService {

    private final WebClientFactory webClientFactory;
    private final InForMationRepository repository;

    @EventListener(ApplicationReadyEvent.class)   // ← PostConstruct 대신
    public void onReady() {
        fetchAndSave();
    }

    @Transactional
    public void fetchAndSave() {
        WebClient client = webClientFactory.create("Information");

        try {
            String response = client.get()
                    .uri(b -> b.path("/getHtfsItem01")
                            .queryParam("serviceKey", "53tJdl6UQo5j8vhnSE27VsFSrcqGypGC2i85Phqih6xywcnFtJjjA3rUTTylcKv41fB4SsCULspZ4M4IKmS6tA==")
                            .queryParam("type", "json")
                            .queryParam("numOfRows", 100)   // 원하면 페이지 늘리기
                            .queryParam("pageNo", 1)
                            .build())
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) return;

            ObjectMapper om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 옵션형
            JsonNode root  = om.readTree(response);

            // body > items (배열)
            JsonNode items = root.path("body").path("items");
            if (!items.isArray()) return;



            for (JsonNode elem : items) {
                JsonNode item = elem.has("item") ? elem.get("item") : elem; // 껍질 제거
                InForMationDto dto = om.treeToValue(item, InForMationDto.class);

                // PK 가드 (필수)
                if (isBlank(dto.getSttemntNo())) {  continue; }

                // 혹시 제품명이 비어있으면 PRDUCT에서 직접 보강 (매핑 누락 대비)
                if (isBlank(dto.getProduct()) && item.hasNonNull("PRDUCT")) {
                    dto.setProduct(item.get("PRDUCT").asText());
                }

                repository.save(dto.toEntity());

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
