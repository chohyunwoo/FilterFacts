package com.example.f_f.food.api.dto;

import com.example.f_f.food.api.entity.InForMation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

// DTO
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InForMationDto {

    @JsonProperty("STTEMNT_NO")
    private String sttemntNo;   // PK

    @JsonProperty("PRDUCT")
    private String product;
    // ← 여기!
    @JsonProperty("ENTRPS")
    private String entrps;

    @JsonProperty("MAIN_FNCTN")
    private String mainFnctn;

    public InForMation toEntity() {
        return InForMation.builder()
                .sttemntNo(sttemntNo)
                .prduct(product)
                .entrps(entrps)
                .mainFnctn(mainFnctn)
                .build();
    }
}
