package com.example.f_f.global.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "hffk_item")
public class HffkItem {

    @Id
    private String ITEM_SEQ;



    private String ENTRPS;
    @Column(columnDefinition = "TEXT")
    private String ENTRPS_ADRES;

    private String ENTRPS_TELNO;
    @Column(columnDefinition = "TEXT")
    private String PRDUCT;
    @Column(columnDefinition = "TEXT")
    private String RTRVL_RESN;

    @Column(columnDefinition = "TEXT")
    private String MNFACTUR_NO;

    private String MNFCTR_DT;

    private String USGPD;

    private String PACKNG_UNIT;

    @Column(columnDefinition = "TEXT")
    private String RTRVL_CMND_DT;

    @Column(columnDefinition = "TEXT")
    private String RM;

    private String BIZRNO;
}