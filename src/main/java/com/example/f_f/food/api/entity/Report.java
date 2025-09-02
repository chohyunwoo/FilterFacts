package com.example.f_f.food.api.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Report_items") // 테이블명 그대로
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "functionality", columnDefinition = "TEXT")
    private String primaryFnclty;  //주된 기능성

    @Column(name = "raw_materials", columnDefinition = "TEXT")
    private String rawmtrlNm;  // 품목유형 (기능지표)

    @Column(name = "product_nm", columnDefinition = "TEXT")
    private String productNm;    // 품목명 4번

    @Column(name = "indiv_nm", columnDefinition = "TEXT")
    private String indivRawmtrlNm; //기능성 원재료

    @Column(name = "matr_cn", columnDefinition = "TEXT")
    private String matrCn;  // 섭취시 주의사항

    @Column(name = "bssh_nm",columnDefinition = "TEXT")
    private String bsshNm ; //업소명
}