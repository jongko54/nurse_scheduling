package com.hist.nursescheduling.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@PlanningEntity
@Table(name = "shift", catalog = "empinfo")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1. 배정될 간호사 (Planning Variable)
    @ManyToOne
    @JoinColumn(name = "nurse_id")
    @PlanningVariable(valueRangeProviderRefs = "nurseRange")
    private Nurse nurse;

    // 2. 근무 시간 정보
    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    // 3. 근무 타입 (Enum)
    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type")
    private ShiftType shiftType;

    @Column(name = "deptCode")
    private String deptCode;

    // 부서 이름을 저장할 필드 추가 (선택 사항)
    private String deptName;

}