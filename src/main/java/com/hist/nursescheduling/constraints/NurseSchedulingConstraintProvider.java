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
                hard_LeaveRequestConflict(factory),
                soft_MaxFiveConsecutiveDays(factory),
                maxNightCountLimit(factory),
                equalDistributeShifts(factory)
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
                // Nurse::getDeptCode 로 그룹화 (Shift 클래스에 getDepartment가 없다면 getNurse().getDeptCode() 사용)
                .groupBy(s -> s.getNurse().getDeptCode(), Shift::getStartDateTime,
                        ConstraintCollectors.toList(Shift::getNurse))
                .filter((dept, start, nurses) -> {
                    // List<Nurse>에서 Senior가 한 명도 없는지 체크
                    return nurses.stream().noneMatch(n -> n.getPosition() != null && n.getPosition().isSenior());
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("숙련자 부재 금지");
    }
    // 4. 월간 나이트 근무 최대치 제한
    Constraint maxNightCountLimit(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .filter(shift -> shift.getShiftType().isNight())
                .groupBy(Shift::getNurse, ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count())
                .filter((nurse, count) -> count > nurse.getMaxNightCount())
                .penalize(HardSoftScore.ONE_HARD, (nurse, count) -> count - nurse.getMaxNightCount())
                .asConstraint("월간 최대 나이트 근무 초과 금지");
    }

    // 5. 같은 부서끼리만 배정 (에러 수정됨: getDepartment -> getDeptCode)
    Constraint hard_DepartmentMatch(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(shift -> shift.getNurse() != null)
                // Shift의 부서(String)와 Nurse의 부서코드(Enum)의 이름을 비교
                .filter(shift -> !shift.getDeptCode().equals(shift.getNurse().getDeptCode().name()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("부서 불일치 배정 금지");
    }

    // 6. 휴가 신청일 근무 배정 금지 (에러 수정됨: getNurse 메서드 활용 가능)
    Constraint hard_LeaveRequestConflict(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(s -> s.getNurse() != null)
                .join(NurseLeaveRequest.class,
                        Joiners.equal(Shift::getNurse, NurseLeaveRequest::getNurse),
                        Joiners.equal(shift -> shift.getStartDateTime().toLocalDate(),
                                NurseLeaveRequest::getRequestedDate))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("휴가 신청일 근무 배정 금지");
    }

    // 1. 간호사 간 근무 일수 평등 배분 (Soft)
    Constraint equalDistributeShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(Shift::getNurse, ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count())
                .penalize(HardSoftScore.ONE_SOFT, (nurse, count) -> count * count)
                .asConstraint("근무 평등 배분 (부하 분산)");
    }

    // 2. 5연근 제한: 5일 연속 근무 페널티 (Soft)
    Constraint soft_MaxFiveConsecutiveDays(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(s -> s.getNurse() != null)
                .join(Shift.class, Joiners.equal(Shift::getNurse))
                .filter((s1, s2) -> ChronoUnit.DAYS.between(s1.getStartDateTime(), s2.getStartDateTime()) == 5)
                .penalize(HardSoftScore.ofSoft(100))
                .asConstraint("5연근 초과 지양");
    }
}