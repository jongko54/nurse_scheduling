package com.hist.nursescheduling.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.hist.nursescheduling.domain.enumNm.ShiftType;
import jakarta.persistence.*;
import lombok.*;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import java.time.LocalDateTime;

@Entity
@Getter // @Data 대신 개별 Getter/Setter 사용 권장
@Setter
@ToString
@NoArgsConstructor
@PlanningEntity
@Table(name = "shift", catalog = "empinfo")
// 핵심: equals와 hashCode를 id 필드에만 한정합니다.
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @PlanningId
    @EqualsAndHashCode.Include // id 값만 비교 및 해시 생성에 사용
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nurse_id")
    @PlanningVariable(valueRangeProviderRefs = "nurseRange")
    private Nurse nurse; // 이 값은 계속 변하므로 hashCode에 포함되면 안 됩니다.

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type")
    private ShiftType shiftType;

    @Column(name = "deptCode")
    private String deptCode;

    private String deptName;
}