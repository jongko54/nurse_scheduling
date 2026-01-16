package com.hist.nursescheduling.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.AllArgsConstructor; // 추가
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList; // 추가
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 생성
@PlanningSolution
public class NurseSchedule {

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "nurseRange")
    private List<Nurse> nurseList;

    @PlanningEntityCollectionProperty
    private List<Shift> shiftList;

    @ProblemFactCollectionProperty
    private List<NurseLeaveRequest> leaveRequestList;

    @PlanningScore
    private HardMediumSoftScore score;

    public NurseSchedule(List<Nurse> nurseList, List<Shift> shiftList) {
        this.nurseList = nurseList;
        this.shiftList = shiftList;
        this.leaveRequestList = new ArrayList<>(); // 빈 리스트로 초기화
    }
}