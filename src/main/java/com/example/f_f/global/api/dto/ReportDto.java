package com.example.f_f.global.api.dto;

import com.example.f_f.global.api.entity.Report;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportDto {

    // 고유 품목제조번호 (PK 역할)
    @JsonProperty("PRDLST_REPORT_NO")
    private String prdlstReportNo;

    // 주된 기능성 (예: 면역력 증진에 도움을 줄 수 있음)
    @JsonProperty("PRIMARY_FNCLTY")
    private String primaryFnclty;

    // 기능지표 성분 (예: 프로바이오틱스 수 1억 이상)
    @JsonProperty("RAWMTRL_NM")
    private String rawmtrlNm;

    // 기능성 원재료 (예: 홍삼농축액, 비타민C)
    @JsonProperty("INDIV_RAWMTRL_NM")
    private String indivRawmtrlNm;

    // 기타 원재료 (첨가제, 부원료)
    @JsonProperty("ETC_RAWMTRL_NM")
    private String etcRawmtrlNm;

    // 캡슐 원재료 (젤라틴, HPMC 등)
    @JsonProperty("CAP_RAWMTRL_NM")
    private String capRawmtrlNm;

    public Report toEntity() {
        return Report.builder()
                .prdlstReportNo(prdlstReportNo)
                .primaryFnclty(primaryFnclty)
                .rawmtrlNm(rawmtrlNm)
                .indivRawmtrlNm(indivRawmtrlNm)
                .etcRawmtrlNm(etcRawmtrlNm)
                .capRawmtrlNm(capRawmtrlNm)
                .build();
    }
}
