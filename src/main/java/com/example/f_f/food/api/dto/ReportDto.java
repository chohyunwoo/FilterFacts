package com.example.f_f.food.api.dto;

import com.example.f_f.food.api.entity.Report;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportDto {

    // 업소명
    @JsonProperty("BSSH_NM")
    private String bsshNm;

    // 주된 기능성 (예: 면역력 증진에 도움을 줄 수 있음)
    @JsonProperty("PRIMARY_FNCLTY")
    private String primaryFnclty;

    // 기능지표 성분 (예: 프로바이오틱스 수 1억 이상)
    @JsonProperty("RAWMTRL_NM")
    private String rawmtrlNm;

    //품목명
    @JsonProperty("PRDLST_NM")
    private  String prdlstNm;

    //섭취시 주의사항
    @JsonProperty("IFTKN_ATNT_MATR_CN")
    private String iftknAttMatrNm;

    //기능성 원재료 .
    @JsonProperty("INDIV_RAWMTRL_NM")
    private String indivRawmtrlNm;

    public Report toEntity() {
        return Report.builder()
                .bsshNm(bsshNm)
                .productNm(prdlstNm)
                .rawmtrlNm(rawmtrlNm)
                .indivRawmtrlNm(indivRawmtrlNm)
                .primaryFnclty(primaryFnclty)
                .matrCn(iftknAttMatrNm)
                .build();
    }
}
