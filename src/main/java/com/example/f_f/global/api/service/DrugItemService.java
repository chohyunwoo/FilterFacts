package com.example.f_f.global.api.service;

import com.example.f_f.global.api.config.WebClientFactory;
import com.example.f_f.global.api.dto.DrugItemDto;
import com.example.f_f.global.api.entity.DrugItem;
import com.example.f_f.global.api.repository.DrugItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class DrugItemService {

    private final WebClientFactory webClientFactory;
    private final DrugItemRepository drugItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${drug.api.service-key}")
    private String serviceKey;

    // @Value("${external-api.apis.drug.base-url}")
    // private String drugBaseUrl;

    @PostConstruct
    public void init() {
        System.out.println("✅ DrugItemService.init() 실행됨");

        // 테스트용 임시 하드코딩
        fetchAndSave("아세트아미노펜");
    }

    public void fetchAndSave(String itemName) {
        WebClient client = webClientFactory.create("drug");

        try {
            // itemName URL 인코딩
            String encodedItemName = URLEncoder.encode(itemName, StandardCharsets.UTF_8);


            // 먼저 URI 만들기
            String baseUrl = "https://apis.data.go.kr/1471000/DrugPrdtPrmsnInfoService06";
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/getDrugPrdtPrmsnDtlInq05")
                    .queryParam("serviceKey", serviceKey) // ✅ 인코딩 안 함
                    .queryParam("type", "json")
                    .build(true) // ✅ false: 직접 넘긴 값은 인코딩하지 않음
                    .toUri();


            // 2️⃣ URI 로그 찍기
            System.out.println("🔗 최종 요청 URI (상대경로): " + uri.toString());

            // 3️⃣ 요청 보내기
            String response = WebClient.create()
                    .get()
                    .uri(uri) // ✅ 최종 URI 직접 사용
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("🔍 drug 응답 원문:\n" + response);

            if (response == null || response.isBlank()) {
                System.out.println("⚠️ 응답이 비어있습니다.");
                return;
            }

            // 혹시 HTML 응답일 경우 예외 처리
            if (response.trim().startsWith("<")) {
                System.err.println("❗ HTML 형식의 오류 응답 수신:\n" + response);
                return;
            }

            // JSON 파싱
            JsonNode root = objectMapper.readTree(response);
            JsonNode itemsNode = root.path("body").path("items");

            if (itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    DrugItemDto dto = objectMapper.treeToValue(itemNode, DrugItemDto.class);
                    System.out.println("✅ 저장 대상: " + dto.getItemName());
                    drugItemRepository.save(convertToEntity(dto));
                }
            } else if (!itemsNode.isMissingNode() && !itemsNode.isNull()) {
                DrugItemDto dto = objectMapper.treeToValue(itemsNode, DrugItemDto.class);
                System.out.println("✅ 저장 대상 (단일): " + dto.getItemName());
                drugItemRepository.save(convertToEntity(dto));
            } else {
                System.out.println("⚠️ 유효한 데이터가 없습니다.");
            }

        } catch (Exception e) {
            System.err.println("❌ API 처리 중 오류 발생");
            e.printStackTrace();
        }
    }

    private DrugItem convertToEntity(DrugItemDto dto) {
        return DrugItem.builder()
                .itemName(dto.getItemName())
                .materialName(dto.getMaterialName())
                .eeDocId(dto.getEeDocId())
                .udDocId(dto.getUdDocId())
                .nbDocId(dto.getNbDocId())
                .validTerm(dto.getValidTerm())
                .cancelDate(dto.getCancelDate())
                .cancelName(dto.getCancelName())
                .changeDate(dto.getChangeDate())
                .mainItemIngr(dto.getMainItemIngr())
                .ingrName(dto.getIngrName())
                .mainIngrEng(dto.getMainIngrEng())
                .rareDrugYn(dto.getRareDrugYn())
                .build();
    }
}