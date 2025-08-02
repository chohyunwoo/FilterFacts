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

    private String itemName;
    private String materialName;
    private String eeDocId;
    private String udDocId;
    private String nbDocId;
    private String validTerm;
    private String cancelDate;
    private String cancelName;
    private String changeDate;
    private String mainItemIngr;
    private String ingrName;
    private String mainIngrEng;
    private String rareDrugYn;
}
