package com.example.f_f.global.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 모든 예외 필드 무시
public class DrugItemDto {
    @JsonProperty("ITEM_NAME")
    private String itemName;

    @JsonProperty("MATERIAL_NAME")
    private String materialName;

    @JsonProperty("EE_DOC_ID")
    private String eeDocId;

    @JsonProperty("UD_DOC_ID")
    private String udDocId;

    @JsonProperty("NB_DOC_ID")
    private String nbDocId;

    @JsonProperty("VALID_TERM")
    private String validTerm;

    @JsonProperty("CANCEL_DATE")
    private String cancelDate;

    @JsonProperty("CANCEL_NAME")
    private String cancelName;

    @JsonProperty("CHANGE_DATE")
    private String changeDate;

    @JsonProperty("MAIN_ITEM_INGR")
    private String mainItemIngr;

    @JsonProperty("INGR_NAME")
    private String ingrName;

    @JsonProperty("MAIN_INGR_ENG")
    private String mainIngrEng;

    @JsonProperty("RARE_DRUG_YN")
    private String rareDrugYn;
}
