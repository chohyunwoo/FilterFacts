package com.example.f_f.global.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {

    private String PTNT_PRDLST_CRTR_CD;
    private String PRDLST_NM;
    private String DRUG_CPNT_KOR_NM;
    private String DRUG_CPNT_ENG_NM;
    private String BSSH_NM;
    private String PTHD_NM;
    private String PTNT_REG_DT;

    @Id
    private String PTNT_NO;
}
