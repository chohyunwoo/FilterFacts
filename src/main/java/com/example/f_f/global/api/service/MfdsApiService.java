package com.example.f_f.global.api.service;

import com.example.f_f.global.api.config.WebClientFactory;
import com.example.f_f.global.api.dto.ItemDto;
import com.example.f_f.global.api.repository.ItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; //JSON 문자열을 JSONNODE ->ITEMDTO로 변환
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class MfdsApiService {

    private final WebClientFactory webClientFactory;
    private final ItemRepository itemRepository;

    @PostConstruct
    public void init() {
        fetchAndSaveData();  // 앱 실행 시 자동 호출
    }


    public void fetchAndSaveData() {
        WebClient client1 = webClientFactory.create("mfds");

        try {
            // ✅ 1. JSON 형식으로 API 호출
            String response = client1.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getDrugDmstPtntStusService")
                            .queryParam("serviceKey", "53tJdl6UQo5j8vhnSE27VsFSrcqGypGC2i85Phqih6xywcnFtJjjA3rUTTylcKv41fB4SsCULspZ4M4IKmS6tA==")
                            .queryParam("type", "json") // ✅ JSON 요청
                            .queryParam("numOfRows", 50) // 원하는 개수 설정
                            .queryParam("pageNo", 1)      // 페이지 설정 (반복 호출에 사용 가능)
                            .build())
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("🔍 응답 원문:\n" + response);

            if (response == null || response.isBlank()) {
                return;
            }

            // ✅ JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("body").path("items");

            if (items.isArray()) {
                for (JsonNode node : items) {
                    ItemDto dto = objectMapper.treeToValue(node, ItemDto.class);
                    itemRepository.save(dto.toEntity());
                }
            } else if (!items.isMissingNode()) {
                ItemDto dto = objectMapper.treeToValue(items, ItemDto.class);
                itemRepository.save(dto.toEntity());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}