package com.example.f_f.global.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Report_items") // 테이블명 그대로
public class Report {


    @Id
    @Column(name = "prdlst_report_no", nullable = false, length = 50)
    private String prdlstReportNo;

    @Column(name = "primary_fnclty", columnDefinition = "TEXT")
    private String primaryFnclty;

    @Column(name = "rawmtrl_nm", columnDefinition = "TEXT")
    private String rawmtrlNm;

    @Column(name = "indiv_rawmtrl_nm", columnDefinition = "TEXT")
    private String indivRawmtrlNm;

    @Column(name = "etc_rawmtrl_nm", columnDefinition = "TEXT")
    private String etcRawmtrlNm;

    @Column(name = "cap_rawmtrl_nm", columnDefinition = "TEXT")
    private String capRawmtrlNm;      // 기능지표성분 (ex: 진세노사이드 Rg1 등)
}