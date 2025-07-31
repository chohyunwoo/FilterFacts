package com.example.f_f.global.api.dto;

import com.example.f_f.global.api.entity.Item;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "item")
public class ItemDto {

    @JacksonXmlProperty(localName = "PTNT_PRDLST_CRTR_CD")
    private String ptnt_PRDLST_CRTR_CD;

    @JacksonXmlProperty(localName = "PRDLST_NM")
    private String PRDLST_NM;

    @JacksonXmlProperty(localName = "DRUG_CPNT_KOR_NM")
    private String DRUG_CPNT_KOR_NM;

    @JacksonXmlProperty(localName = "DRUG_CPNT_ENG_NM")
    private String DRUG_CPNT_ENG_NM;

    @JacksonXmlProperty(localName = "BSSH_NM")
    private String BSSH_NM;

    @JacksonXmlProperty(localName = "PTHD_NM")
    private String pthd_NM;

    @JacksonXmlProperty(localName = "PTNT_REG_DT")
    private String PTNT_REG_DT;

    @JacksonXmlProperty(localName = "PTNT_NO")
    private String ptnt_NO;

    public Item toEntity() {
        return Item.builder()
                .PRDLST_NM(PRDLST_NM)
                .DRUG_CPNT_KOR_NM(DRUG_CPNT_KOR_NM)
                .DRUG_CPNT_ENG_NM(DRUG_CPNT_ENG_NM)
                .BSSH_NM(BSSH_NM)
                .PTNT_REG_DT(PTNT_REG_DT)
                .PTNT_NO(ptnt_NO)
                .PTHD_NM(pthd_NM)
                .PTNT_PRDLST_CRTR_CD(ptnt_PRDLST_CRTR_CD)
                .build();
    }
}
