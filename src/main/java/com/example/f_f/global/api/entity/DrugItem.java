package com.example.f_f.global.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drug_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DrugItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String itemName;
    @Column(columnDefinition = "TEXT")
    private String materialName;
    @Column(columnDefinition = "TEXT")
    private String eeDocId;
    @Column(columnDefinition = "TEXT")
    private String udDocId;
    @Column(columnDefinition = "TEXT")
    private String nbDocId;
    @Column(columnDefinition = "TEXT")
    private String validTerm;
    private String cancelDate;
    private String cancelName;
    private String changeDate;
    @Column(columnDefinition = "TEXT")
    private String mainItemIngr;
    @Column(columnDefinition = "TEXT")
    private String ingrName;
    @Column(columnDefinition = "TEXT")
    private String mainIngrEng;
    private String rareDrugYn;
}
