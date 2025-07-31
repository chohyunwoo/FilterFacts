package com.example.f_f.global.api.service;

import com.example.f_f.global.api.config.WebClientFactory;
import com.example.f_f.global.api.dto.HffkItemDto;
import com.example.f_f.global.api.repository.HffkItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class HffkApiService {

    private final WebClientFactory webClientFactory;
    private final HffkItemRepository hffkItemRepository;

    @PostConstruct
    public void init() {
        System.out.println("✅ HffkApiService.init() 실행됨");
        fetchAndSave();
    }

    public void fetchAndSave() {
        WebClient client = webClientFactory.create("hffk");

        try {
            String response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getMdcinRtrvlSleStpgeEtcItem03")
                            .queryParam("serviceKey", "53tJdl6UQo5j8vhnSE27VsFSrcqGypGC2i85Phqih6xywcnFtJjjA3rUTTylcKv41fB4SsCULspZ4M4IKmS6tA==")
                            .queryParam("type", "json")
                            .build())
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("🔍 응답 원문:\n" + response);

            if (response == null || response.isBlank()) return;

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("body").path("items");

            if (items.isArray()) {
                for (JsonNode wrapper : items) {
                    JsonNode itemNode = wrapper.path("item");
                    if (!itemNode.isMissingNode()) {
                        HffkItemDto dto = objectMapper.treeToValue(itemNode, HffkItemDto.class);
                        System.out.println("✅ 저장 대상: " + dto.getITEM_SEQ());
                        hffkItemRepository.save(dto.toEntity());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}