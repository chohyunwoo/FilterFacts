package com.example.f_f.global.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notified_drug_efficacy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifiedDrugEfficacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingredient_name", columnDefinition = "TEXT")
    private String ingredientName;

    @Column(name = "efficacy", columnDefinition = "TEXT")
    private String efficacy;

    @Column(name = "precautions", columnDefinition = "TEXT")
    private String precautions;

    @Column(name = "dosage", columnDefinition = "TEXT")
    private String dosage;

    @Column(name = "drug_name", length = 500)
    private String drugName;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}