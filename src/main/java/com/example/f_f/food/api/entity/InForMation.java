package com.example.f_f.food.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "information_items") // 테이블명 그대로
public class InForMation {

    // 품목제조관리번호 (PK)
    @Id
    @Column(name = "product_report_no", length = 50, nullable = false)
    private String sttemntNo;

    // 제품명
    @Column(name = "prduct", length = 200)
    private String prduct;

    // 업체명
    @Column(name = "ltd", length = 200)
    private String entrps;

    // 주된 기능성 (효능)
    @Column(name = "functionalities", columnDefinition = "TEXT")
    private String mainFnctn;

}
