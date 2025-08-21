package com.example.f_f.food.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "RawMaterial_items") // 테이블명 그대로
public class RawMaterial {

    @Id   // 인정번호
    @Column(name = "hf_fnclty_mtral_rcogn_no", nullable = false, length = 50)
    private String hf_fnclty_mtral_rcogn_no;

    //업체명
    @Column(name = "bssh_nm", columnDefinition = "TEXT")
    private String bssh_nm;

    //업종
    @Column(name = "induty_nm", columnDefinition = "TEXT")
    private String induty_nm;

    //신청 원료명
    @Column(name = "aplc_rawmtrl_nm", columnDefinition = "TEXT")
    private String aplc_rawmtrl_nm;

    //기능성 내용
    @Column(name = "fnclty_cn", columnDefinition = "TEXT")
    private String fnclty_cn;

}
