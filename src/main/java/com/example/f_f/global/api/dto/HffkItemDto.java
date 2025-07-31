package com.example.f_f.global.api.dto;

import com.example.f_f.global.api.entity.HffkItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HffkItemDto {

    @JsonProperty("ENTRPS")
    private String ENTRPS;

    @JsonProperty("ENTRPS_ADRES")
    private String ENTRPS_ADRES;

    @JsonProperty("ENTRPS_TELNO")
    private String ENTRPS_TELNO;

    @JsonProperty("PRDUCT")
    private String PRDUCT;

    @JsonProperty("RTRVL_RESN")
    private String RTRVL_RESN;

    @JsonProperty("MNFCTUR_NO") // ✅ 여기가 핵심
    private String MNFCTUR_NO;

    @JsonProperty("MNFCTUR_DT")
    private String MNFCTUR_DT;

    @JsonProperty("USGPD")
    private String USGPD;

    @JsonProperty("PACKNG_UNIT")
    private String PACKNG_UNIT;

    @JsonProperty("RTRVL_CMMND_DT")
    private String RTRVL_CMMND_DT;

    @JsonProperty("RM")
    private String RM;

    @JsonProperty("ITEM_SEQ")
    private String ITEM_SEQ;

    @JsonProperty("BIZRNO")
    private String BIZRNO;


    public HffkItem toEntity() {
        return HffkItem.builder()
                .ENTRPS(ENTRPS)
                .ENTRPS_ADRES(ENTRPS_ADRES)
                .ENTRPS_TELNO(ENTRPS_TELNO)
                .PRDUCT(PRDUCT)
                .RTRVL_RESN(RTRVL_RESN)
                .MNFACTUR_NO(MNFCTUR_NO)
                .MNFCTR_DT(MNFCTUR_DT)
                .USGPD(USGPD)
                .PACKNG_UNIT(PACKNG_UNIT)
                .RTRVL_CMND_DT(RTRVL_CMMND_DT)
                .RM(RM)
                .ITEM_SEQ(ITEM_SEQ)
                .BIZRNO(BIZRNO)
                .build();
    }
}
