package com.example.f_f.global.api.service;

import com.example.f_f.global.api.config.WebClientFactory;
import com.example.f_f.global.api.dto.NotifiedDrugEfficacyDto;
import com.example.f_f.global.api.repository.NotifiedDrugEfficacyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class NotifiedDrugEfficacyApiService {

    private final WebClientFactory webClientFactory;
    private final NotifiedDrugEfficacyRepository notifiedDrugEfficacyRepository;

    @PostConstruct
    public void init() {
        System.out.println("✅ NotifiedDrugEfficacyApiService.init() 실행됨");
        fetchAndSave();
    }

    public void fetchAndSave() {
        WebClient client = webClientFactory.create("efficacy");

        try {
            String response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getRareSelfmdcin")
                            .queryParam("serviceKey", "53tJdl6UQo5j8vhnSE27VsFSrcqGypGC2i85Phqih6xywcnFtJjjA3rUTTylcKv41fB4SsCULspZ4M4IKmS6tA==")
                            .queryParam("type", "json")
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 100)
                            .build())
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("🔍nofifiedDrug 응답 원문:\n" + response);

            if (response == null || response.isBlank()) return;

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("body").path("items");

            if (items.isArray()) {
                for (JsonNode wrapper : items) {
                    JsonNode itemNode = wrapper.path("item");
                    if (!itemNode.isMissingNode()) {
                        NotifiedDrugEfficacyDto dto = objectMapper.treeToValue(itemNode, NotifiedDrugEfficacyDto.class);
                        System.out.println("✅ 저장 대상: " + dto.getDrugName());
                        notifiedDrugEfficacyRepository.save(dto.toEntity());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}