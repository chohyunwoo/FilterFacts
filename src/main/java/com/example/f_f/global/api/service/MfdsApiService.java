package com.example.f_f.global.api.service;

import com.example.f_f.global.api.config.WebClientFactory;
import com.example.f_f.global.api.dto.ItemDto;
import com.example.f_f.global.api.entity.Item;
import com.example.f_f.global.api.repository.ItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
        WebClient client = webClientFactory.create("mfds");

        try {
            // ✅ 1. API 호출
            String response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getDrugDmstPtntStusService")
                            .queryParam("serviceKey", "53tJdl6UQo5j8vhnSE27VsFSrcqGypGC2i85Phqih6xywcnFtJjjA3rUTTylcKv41fB4SsCULspZ4M4IKmS6tA==")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("🔍 응답 원문:\n" + response);

            if (response == null || !response.trim().startsWith("<")) {
                System.out.println("❌ XML 응답이 아닙니다. 파싱 중단.");
                return;
            }

            // ✅ 2. XML 파싱
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode root = xmlMapper.readTree(response);

            // ✅ 3. item 위치 추적 (response > body > items > item)
            JsonNode items = root.path("body").path("items").path("item");

            System.out.println("📌 item 노드 추출: " + items);
            System.out.println("📌 item.isArray(): " + items.isArray());

            if (items.isArray()) {
                for (JsonNode node : items) {
                    ItemDto dto = xmlMapper.treeToValue(node, ItemDto.class);
                    System.out.println("✅ DTO 변환 성공: " + dto);

                    Item entity = dto.toEntity();
                    itemRepository.save(entity);
                    System.out.println("✅ 저장 완료: " + entity.getPTNT_NO());
                }
            } else if (!items.isMissingNode()) {
                // 단일 item일 경우
                ItemDto dto = xmlMapper.treeToValue(items, ItemDto.class);
                Item entity = dto.toEntity();
                itemRepository.save(entity);
                System.out.println("✅ 단일 항목 저장 완료: " + entity.getPTNT_NO());
            } else {
                System.out.println("⚠️ 'item' 노드가 존재하지 않습니다.");
            }

        } catch (Exception e) {
            System.out.println("❌ 예외 발생:");
            e.printStackTrace();
        }
    }
}
