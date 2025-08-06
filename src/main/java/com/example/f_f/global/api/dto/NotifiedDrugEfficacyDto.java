package com.example.f_f.global.api.dto;

import com.example.f_f.global.api.entity.NotifiedDrugEfficacy;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // 모든 예외 필드 무시
public class NotifiedDrugEfficacyDto {

    @JsonProperty("INGR")  // 성분명
    private String ingredientName;

    @JsonProperty("EFFICACY")    // 효능효과
    private String efficacy;

    @JsonProperty("NB")    // 주의사항
    private String precautions;

    @JsonProperty("UD")    // 용법용량
    private String dosage;

    @JsonProperty("MEDCIN_NAME")  // 약품명
    private String drugName;

    public NotifiedDrugEfficacy toEntity() {
        return NotifiedDrugEfficacy.builder()
                .ingredientName(ingredientName)
                .efficacy(efficacy)
                .precautions(precautions)
                .dosage(dosage)
                .drugName(drugName)
                .build();
    }
}