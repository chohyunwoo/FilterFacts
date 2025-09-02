package com.example.f_f.food.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Material_items") // 테이블명 그대로
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bssh_nm", length = 200)
    private String bsshNm;  // 업체명

    @Column(name = "product_nm", columnDefinition = "TEXT")
    private String productNm;         // 품목명

    @Column(name = "raw_materials", length = 1000)
    private String rawmtrlNm;        // 원재료

    @Column(name = "functionality", length = 1000)
    private String primaryFnclty;    // 주된 기능성

    @Column(name = "matr_cn", columnDefinition = "TEXT")
    private String matrCn;  // 섭취시 주의사항

}

