package com.example.f_f.global.api.dto;

import com.example.f_f.global.api.entity.Item;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "item")
public class ItemDto {

    @JsonProperty("PTNT_PRDLST_CRTR_CD")
    private String PTNT_PRDLST_CRTR_CD;  // 특허 제품 기준 코드

    @JsonProperty("PRDLST_NM")
    private String PRDLST_NM;  // 제품명

    @JsonProperty("DRUG_CPNT_KOR_NM")
    private String DRUG_CPNT_KOR_NM;  // 주성분명(한글)

    @JsonProperty("DRUG_CPNT_ENG_NM")
    private String DRUG_CPNT_ENG_NM;  // 주성분명(영문)

    @JsonProperty("BSSH_NM")
    private String BSSH_NM;  // 업체명

    @JsonProperty("PTHD_NM")
    private String PTHD_NM;  // 출원자명

    @JsonProperty("PTNT_REG_DT")
    private String PTNT_REG_DT;  // 특허 등록일

    @JsonProperty("PTNT_NO")
    private String PTNT_NO;  // 특허번호

/*
API 응답 -> DTO 파싱 -> DTO를 엔티티로 변환
 */
    public Item toEntity() {
        return Item.builder()
                .PRDLST_NM(PRDLST_NM)
                .DRUG_CPNT_KOR_NM(DRUG_CPNT_KOR_NM)
                .DRUG_CPNT_ENG_NM(DRUG_CPNT_ENG_NM)
                .BSSH_NM(BSSH_NM)
                .PTNT_REG_DT(PTNT_REG_DT)
                .PTNT_NO(PTNT_NO)
                .PTHD_NM(PTHD_NM)
                .PTNT_PRDLST_CRTR_CD(PTNT_PRDLST_CRTR_CD)
                .build();
    }
}
