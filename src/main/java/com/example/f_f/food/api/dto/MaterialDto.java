package com.example.f_f.food.api.dto;

import com.example.f_f.food.api.entity.Material;
import com.example.f_f.food.api.entity.Report;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaterialDto {

        @JsonProperty("PRDLST_NM")
        private String prdlstNm;          // 품목명

        @JsonProperty("PRIMARY_FNCLTY")
        private String primaryFnclty;     // 주된 기능성

        @JsonProperty("BSSH_NM")
        private String bsshNm;           // 업체명

        @JsonProperty("RAWMTRL_NM")
        private String rawmtrlNm;         // 원재료

        @JsonProperty("IFTKN_ATNT_MATR_CN")
        private String iftknAtnMatrCn;  // 섭취시 주의사항

    public Material toEntity() {
        return Material.builder()
                .bsshNm(bsshNm)
                .productNm(prdlstNm)
                .rawmtrlNm(rawmtrlNm)
                .primaryFnclty(primaryFnclty)
                .matrCn(iftknAtnMatrCn)
                .build();
    }
}
