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


    public Report toEntity() {
        return Report.builder()
                .prdlstReportNo(prdlstReportNo)
                .primaryFnclty(primaryFnclty)
                .rawmtrlNm(rawmtrlNm)
                .build();
    }
}
