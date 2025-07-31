package com.example.f_f.global.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity // 이 클래스가 JPA 엔티티임을 선언 (DB 테이블로 매핑됨)
@Getter
@Setter
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 생성
@NoArgsConstructor  // 기본 생성자 생성
@Builder            // 빌더 패턴으로 객체 생성 가능
public class Item {

    private String PTNT_PRDLST_CRTR_CD; // 특허 제품 기준 코드

    private String PRDLST_NM; // 제품명

    private String DRUG_CPNT_KOR_NM; // 주성분명(한글)

    private String DRUG_CPNT_ENG_NM; // 주성분명(영문)

    private String BSSH_NM; // 업체명

    private String PTHD_NM; // 출원자명

    private String PTNT_REG_DT; // 특허 등록일

    @Id
    private String PTNT_NO; // 특허번호 (기본키로 설정)

}
