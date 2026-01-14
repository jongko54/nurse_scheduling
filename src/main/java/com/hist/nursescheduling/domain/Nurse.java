package com.hist.nursescheduling.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "nurse")
public class Nurse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String employeeNum; // 사번

    private String name;

    @Enumerated(EnumType.STRING)
    private Position position; // HN, CN, SN, NJ

    @Enumerated(EnumType.STRING)
    private Department deptCode; // MDINTM, CTNICU 등

    private String deptName; // "내과", "신생아중환자실" 등 (조인 방지용)

    @Enumerated(EnumType.STRING)
    private TeamCode teamCode; // NSIPT1, NSSPC1 등

    private String teamNm; // "입원간호1팀" 등

    private LocalDate hireDate; // 숙련도 산출용

    private Integer maxNightCount = 7; // 기본값 7개 제한

    private boolean isActive = true; // 현재 스케줄링 대상인지 여부
}