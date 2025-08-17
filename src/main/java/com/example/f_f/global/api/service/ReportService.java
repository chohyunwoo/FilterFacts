package com.example.f_f.global.api.service;

import com.example.f_f.global.api.config.WebClientFactory;
import com.example.f_f.global.api.dto.ReportDto;
import com.example.f_f.global.api.repository.ReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final WebClientFactory webClientFactory;
    private final ReportRepository reportRepository;

    @PostConstruct
    public void init() {
        System.out.println("✅ Report.init() 실행됨");
        fetchAndSave();
    }

    @org.springframework.transaction.annotation.Transactional  // ← 트랜잭션 보장
    public void fetchAndSave() {
        WebClient client = webClientFactory.create("Report");

        try {
            String response = client.get()
                    .uri(b -> b.pathSegment(
                            "api",
                            "2da40e9812a643ebbdc6",    // 환경변수/설정으로 빼세요
                            "I0030",                  // ← 실제 서비스ID (스샷 JSON 루트 키가 I0030로 보임)
                            "json",
                            "1",
                            "100"
                    ).build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("🔍 응답 원문(length): " + (response == null ? 0 : response.length()));
            if (response == null || response.isBlank() || response.trim().startsWith("<")) {
                throw new IllegalStateException("API 오류(HTML 응답): " + response);
            }

            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(response);

            // 1) 서비스ID를 동적으로 잡아 안전하게 파싱 (I0030 등)
            String serviceId = root.fieldNames().hasNext() ? root.fieldNames().next() : null;
            if (serviceId == null) throw new IllegalStateException("루트에 서비스ID 키가 없습니다: " + root.toPrettyString());

            JsonNode rows = root.path(serviceId).path("row");
            if (!rows.isArray()) {
                System.out.println("⚠️ row 배열이 없습니다. 실제 구조: " + root.toPrettyString());
                return;
            }

            int success = 0, fail = 0;
            for (JsonNode item : rows) {
                try {
                    // TODO: ReportDto 필드명이 JSON 키와 맞는지 꼭 확인 (@JsonProperty 사용 권장)
                    ReportDto dto = om.treeToValue(item, ReportDto.class);
                    reportRepository.save(dto.toEntity());
                    success++;
                } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                    fail++;
                    System.out.println("❌ 제약 위반으로 스킵: " + item.toString());
                    ex.printStackTrace();
                } catch (Exception ex) {
                    fail++;
                    System.out.println("❌ 매핑/저장 오류로 스킵: " + item.toString());
                    ex.printStackTrace();
                }
            }

            // 2) 즉시 flush 해서 실제 insert 되었는지 보장
            // (JpaRepository라면 flush 메서드가 있을 수도)
            // reportRepository.flush();  // JpaRepository<..., ...> 가 Flushable 지원 시

            long count = reportRepository.count();
            System.out.printf("✅ 저장 완료: 성공 %d, 실패 %d, 현재 테이블 건수=%d%n", success, fail, count);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
