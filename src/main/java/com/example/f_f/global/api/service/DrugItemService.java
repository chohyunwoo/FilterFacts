package com.example.f_f.global.api.service;

import com.example.f_f.global.api.config.WebClientFactory;
import com.example.f_f.global.api.dto.DrugItemDto;
import com.example.f_f.global.api.entity.DrugItem;
import com.example.f_f.global.api.repository.DrugItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DrugItemService {

    private final WebClientFactory webClientFactory;
    private final DrugItemRepository drugItemRepository;

    @PostConstruct
    public void init() {
        System.out.println("✅ DrugItemService.init() 실행됨");
        fetchAndSave();
    }

    public void fetchAndSave() {
        WebClient client = webClientFactory.create("drug");

        try {
            String response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getDrugPrdtPrmsnDtlInq05")
                            .queryParam("serviceKey", "53tJdl6UQo5j8vhnSE27VsFSrcqGypGC2i85Phqih6xywcnFtJjjA3rUTTylcKv41fB4SsCULspZ4M4IKmS6tA==")
                            .queryParam("type", "json")
                            .queryParam("numOfRows", 100) // 원하는 개수 설정
                            .queryParam("pageNo", 1)      // 페이지 설정 (반복 호출에 사용 가능)
                            .build())
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("🔍 drug 응답 원문:\n" + response);

            if (response == null || response.isBlank()) return;

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            JsonNode itemNode = root.path("body").path("items");

            List<DrugItem> list = new ArrayList<>();

            if (itemNode.isArray()) {
                System.out.println("📦 itemNode는 배열입니다.");
                for (JsonNode node : itemNode) {
                    DrugItemDto dto = objectMapper.treeToValue(node, DrugItemDto.class);
                    System.out.println("✅ DTO → " + dto.getItemName());
                    list.add(dto.toEntity());
                }
            } else if (!itemNode.isMissingNode() && !itemNode.isNull()) {
                System.out.println("📦 itemNode는 단일 객체입니다.");
                DrugItemDto dto = objectMapper.treeToValue(itemNode, DrugItemDto.class);
                list.add(dto.toEntity());
            } else {
                System.out.println("⚠️ itemNode가 없습니다.");
                return;
            }

            // ✅ DB 저장
            drugItemRepository.saveAll(list);
            System.out.println("✅ 총 " + list.size() + "개 저장 완료");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
