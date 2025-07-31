package com.example.f_f.global.api.service;

import com.example.f_f.global.api.config.WebClientFactory;
import com.example.f_f.global.api.dto.DrugItemDto;
import com.example.f_f.global.api.entity.DrugItem;
import com.example.f_f.global.api.repository.DrugItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugItemService {

    private final WebClientFactory webClientFactory;
    private final DrugItemRepository drugItemRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${drug.api.service-key}")
    private String serviceKey;

    @Value("${external-api.apis.drug.base-url}")
    private String drugBaseUrl;

    public void fetchAndSaveDrugItems(String itemName, String startDate, String endDate, Integer pageNo, Integer numOfRows) {
        try {
            WebClient client = webClientFactory.create("drug");

            String encodedItemName = URLEncoder.encode(itemName, StandardCharsets.UTF_8);

            URI uri = UriComponentsBuilder
                    .fromUriString(drugBaseUrl)
                    .path("/getDrugPrdtPrmsnDtlInq05")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("type", "json")
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("item_name", encodedItemName)
                    .queryParam("start_change_date", startDate)
                    .queryParam("end_change_date", endDate)
                    .build(true)
                    .toUri();

            System.out.println("🔗 요청 URI: " + uri);

            String response = client.get()
                    .uri(uri)
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("✅ 응답 원문:\n" + response);

            JsonNode root = objectMapper.readTree(response);
            JsonNode itemsNode = root.path("body").path("items");
            System.out.println("itemsNode = " + itemsNode);

            List<DrugItem> list = new ArrayList<>();

            if (itemsNode.isArray()) {
                System.out.println("itemsNode는 배열");
                for (JsonNode itemNode : itemsNode) {
                    System.out.println("itemNode = " + itemNode);
                    // json 역직렬화
                    DrugItemDto dto = objectMapper.treeToValue(itemNode, DrugItemDto.class);
                    System.out.println("dto = " + dto);
                    list.add(convertToEntity(dto));
                }
            } else if (!itemsNode.isMissingNode() && !itemsNode.isNull()) {
                System.out.println("itemsNode가 일단 배열은 아니고 null도 아님");
                DrugItemDto dto = objectMapper.treeToValue(itemsNode, DrugItemDto.class);
                list.add(convertToEntity(dto));
            }


            System.out.println("list.isEmpty() = " + list.isEmpty());
            for (DrugItem drugItem : list) {
                System.out.println("drugItem = " + drugItem);
            }




            if (!list.isEmpty()) {
                drugItemRepository.saveAll(list);
                System.out.println("💾 저장된 항목 수: " + list.size());
            } else {
                System.out.println("⚠️ 저장할 데이터가 없습니다.");
            }

        } catch (Exception e) {
            System.err.println("❌ API 데이터 처리 중 오류 발생");
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