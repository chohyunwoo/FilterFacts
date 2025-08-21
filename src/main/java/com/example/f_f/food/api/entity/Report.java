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
    @Column(name = "product_report_no", nullable = false, length = 50)
    private String prdlstReportNo;

    @Column(name = "functionality", columnDefinition = "TEXT")
    private String primaryFnclty;

    @Column(name = "raw_materials", columnDefinition = "TEXT")
    private String rawmtrlNm;

}