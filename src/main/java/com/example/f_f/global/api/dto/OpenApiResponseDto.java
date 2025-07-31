package com.example.f_f.global.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OpenApiResponseDto {
    private ResponseBody body;

    @Getter @Setter
    public static class ResponseBody {
        private Integer numOfRows;
        private Integer pageNo;
        private Integer totalCount;
        private ResponseItems items;
    }

    @Getter @Setter
    public static class ResponseItems {
        private List<PatentItem> item;
    }

    @Getter @Setter
    public static class PatentItem {
        private String PTNT_PRDLST_CRTR_CD;
        private String PRDLST_NM;
        private String DRUG_CPNT_KOR_NM;
        private String DRUG_CPNT_ENG_NM;
        private String BSSH_NM;
        private String PTHD_NM;
        private String PTNT_REG_DT;
        private String PTNT_NO;
    }
}