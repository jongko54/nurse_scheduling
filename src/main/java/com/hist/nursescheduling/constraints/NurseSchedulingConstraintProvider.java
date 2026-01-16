package com.hist.nursescheduling.constraints;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import com.hist.nursescheduling.domain.NurseLeaveRequest;
import com.hist.nursescheduling.domain.Shift;
import java.time.temporal.ChronoUnit;

public class NurseSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                hard_NoOverlappingShifts(factory),
                hard_NightAfterDayEveningProhibited(factory),
                hard_SeniorRequiredPerShift(factory),
                hard_DepartmentMatch(factory),
                hard_LeaveRequestConflict(factory),
                hard_MinStaffing(factory),
                soft_MaxFiveConsecutiveDays(factory),
                maxNightCountLimit(factory),
                soft_equalDistributeShifts(factory)
        };
    }

    // 1. 중복 근무 금지: 한 간호사가 동일 시간에 중복 배정 불가
    Constraint hard_NoOverlappingShifts(ConstraintFactory factory) {
        return factory.forEachUniquePair(Shift.class,
                        Joiners.equal(Shift::getNurse),
                        Joiners.overlapping(Shift::getStartDateTime, Shift::getEndDateTime))
                .filter((s1, s2) -> s1.getNurse() != null)
                .penalize(HardMediumSoftScore.ONE_HARD)
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
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Night 후 Day Evening 금지");
    }

    // 3. 시프트 숙련자 필수
    Constraint hard_SeniorRequiredPerShift(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(s -> s.getNurse() != null)
                .groupBy(Shift::getDeptCode, Shift::getStartDateTime,
                        // countFiltered 대신 sum을 사용하여 숙련자(Senior)인 경우 1점씩 합산
                        ConstraintCollectors.sum(s -> s.getNurse().getPosition().isSenior() ? 1 : 0))
                .filter((dept, start, seniorCount) -> seniorCount == 0)
                .penalize(HardMediumSoftScore.ofHard(100))
                .asConstraint("시프트 내 숙련자 필수 배치");
    }

    // 4. 월간 나이트 근무 최대치 제한
    Constraint maxNightCountLimit(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .filter(shift -> shift.getShiftType().isNight())
                .groupBy(Shift::getNurse, ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count())
                .filter((nurse, count) -> count > nurse.getMaxNightCount())
                .penalize(HardMediumSoftScore.ONE_HARD, (nurse, count) -> count - nurse.getMaxNightCount())
                .asConstraint("월간 최대 나이트 근무 초과 금지");
    }

    // 5. 같은 부서끼리만 배정 (에러 수정됨: getDepartment -> getDeptCode)
    Constraint hard_DepartmentMatch(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(shift -> shift.getNurse() != null)
                .filter(shift -> !shift.getDeptCode().equals(shift.getNurse().getDeptCode().name()))
                .penalize(HardMediumSoftScore.ofHard(1000)) // 매우 강력한 페널티
                .asConstraint("부서 불일치 배정 금지");
    }

    // 6. 휴가 신청일 근무 배정 금지 (에러 수정됨: getNurse 메서드 활용 가능)
    Constraint hard_LeaveRequestConflict(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .join(NurseLeaveRequest.class,
                        Joiners.equal(Shift::getNurse, NurseLeaveRequest::getNurse),
                        Joiners.equal(shift -> shift.getStartDateTime().toLocalDate(),
                                NurseLeaveRequest::getRequestedDate))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Off 신청 날짜에는 근무 배정 금지");
    }

    Constraint hard_MinStaffing(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(shift -> shift.getNurse() == null) // 간호사가 없는 빈 슬롯 발견 시
                .penalize(HardMediumSoftScore.ofHard(1000)) // 강력한 페널티 부여
                .asConstraint("필수 인원 미충족 금지");
    }

    // 1. 간호사 간 근무 일수 평등 배분 (Soft)
    Constraint soft_equalDistributeShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(Shift::getNurse, ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count())
                .penalize(HardMediumSoftScore.ONE_SOFT, (nurse, count) -> count * count)
                .asConstraint("근무 평등 배분 (부하 분산)");
    }

    // 2. 5연근 제한: 5일 연속 근무 페널티 (Soft)
    Constraint soft_MaxFiveConsecutiveDays(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(s -> s.getNurse() != null)
                .join(Shift.class, Joiners.equal(Shift::getNurse))
                .filter((s1, s2) -> ChronoUnit.DAYS.between(s1.getStartDateTime(), s2.getStartDateTime()) == 5)
                .penalize(HardMediumSoftScore.ofSoft(100))
                .asConstraint("5연근 초과 지양");
    }

}