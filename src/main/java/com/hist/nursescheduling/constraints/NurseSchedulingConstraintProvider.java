package com.hist.nursescheduling.constraints;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import com.hist.nursescheduling.domain.NurseLeaveRequest;
import com.hist.nursescheduling.domain.Shift;
import java.time.temporal.ChronoUnit;

public class NurseSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                hard_NoOverlappingShifts(factory),
                hard_NightAfterDayEveningProhibited(factory),
                hard_SeniorRequiredPerShift(factory),
                hard_DepartmentMatch(factory),
                soft_MaxFiveConsecutiveDays(factory),
                soft_OffRequestMatch(factory)
        };
    }

    // 1. 중복 근무 금지: 한 간호사가 동일 시간에 중복 배정 불가
    Constraint hard_NoOverlappingShifts(ConstraintFactory factory) {
        return factory.forEachUniquePair(Shift.class,
                        Joiners.equal(Shift::getNurse),
                        Joiners.overlapping(Shift::getStartDateTime, Shift::getEndDateTime))
                .filter((s1, s2) -> s1.getNurse() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("중복 근무 금지");
    }

    // 2. N-D/E 금지: 밤근무 다음날 낮/저녁 근무 금지
    Constraint hard_NightAfterDayEveningProhibited(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(s -> s.getNurse() != null && s.getShiftType() != null && s.getShiftType().isNight())
                .join(Shift.class,
                        Joiners.equal(Shift::getNurse),
                        Joiners.equal(s -> s.getStartDateTime().toLocalDate().plusDays(1),
                                s -> s.getStartDateTime().toLocalDate()))
                .filter((s1, s2) -> s2.getShiftType().isDayOrEvening())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Night 후 Day/Evening 금지");
    }

    // 3. 숙련자 배치: 시프트별 최소 1명의 HN/CN 포함
    Constraint hard_SeniorRequiredPerShift(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(s -> s.getNurse() != null)
                .groupBy(Shift::getDepartment, Shift::getStartDateTime,
                        ConstraintCollectors.toList(Shift::getNurse))
                .filter((dept, start, nurses) -> nurses.stream().noneMatch(n -> n.getPosition() != null && n.getPosition().isSenior()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("숙련자 부재 금지");
    }

    // 4. 같은 부서끼리만 배정
    Constraint hard_DepartmentMatch(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(shift -> shift.getNurse() != null)
                .filter(shift -> !shift.getDepartment().equals(shift.getNurse().getDepartment()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("부서 불일치 배정 금지");
    }

    // 4. 5연근 제한: 5일 연속 근무 페널티 (단순화된 버전)
    Constraint soft_MaxFiveConsecutiveDays(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(s -> s.getNurse() != null)
                .join(Shift.class, Joiners.equal(Shift::getNurse))
                .filter((s1, s2) -> ChronoUnit.DAYS.between(s1.getStartDateTime(), s2.getStartDateTime()) == 5)
                .penalize(HardSoftScore.ofSoft(100))
                .asConstraint("5연근 초과 지양");
    }

    // 5. 오프 신청 반영: NurseLeaveRequest와 대조
    Constraint soft_OffRequestMatch(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(s -> s.getNurse() != null)
                .join(NurseLeaveRequest.class,
                        Joiners.equal(Shift::getNurse, NurseLeaveRequest::getNurse),
                        Joiners.equal(s -> s.getStartDateTime().toLocalDate(),
                                NurseLeaveRequest::getRequestedDate))
                .penalize(HardSoftScore.ofSoft(500))
                .asConstraint("오프 신청 미준수");
    }
}