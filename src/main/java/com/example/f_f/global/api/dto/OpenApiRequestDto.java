package com.example.f_f.global.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenApiRequestDto {
    private String serviceKey;
    private Integer pageNo ;
    private Integer numOfRows ;
    private String type ;
    private String PRDLST_NM;
    private String PTNT_NO;
}
