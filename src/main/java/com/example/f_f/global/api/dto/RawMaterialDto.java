package com.example.f_f.global.api.dto;

import com.example.f_f.global.api.entity.RawMaterial;
import com.example.f_f.global.api.entity.Report;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawMaterialDto {

    @JsonProperty("HF_FNCLTY_MTRAL_RCOGN_NO")  // 인정번호
    private String hf_fnclty_mtral_rcogn_no;

    //업체명
    @JsonProperty("BSSH_NM")
    private String bssh_nm;

    //업종
    @JsonProperty("INDUTY_NM")
    private String induty_nm;

    //신청 원료명
    @JsonProperty("APLC_RAWMTRL_NM")
    private String aplc_rawmtrl_nm;

    //기능성 내용
    @JsonProperty("FNCLTY_CN")
    private String fnclty_cn;


    public RawMaterial toEntity() {
        return RawMaterial.builder()
                .hf_fnclty_mtral_rcogn_no(hf_fnclty_mtral_rcogn_no)
                .bssh_nm(bssh_nm)
                .induty_nm(induty_nm)
                .aplc_rawmtrl_nm(aplc_rawmtrl_nm)
                .fnclty_cn(fnclty_cn)
                .build();
    }
}