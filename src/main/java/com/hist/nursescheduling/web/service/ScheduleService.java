package com.hist.nursescheduling.web.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.hist.nursescheduling.domain.*;
import com.hist.nursescheduling.repository.NurseLeaveRequestRepository;
import com.hist.nursescheduling.repository.NurseRepository;
import com.hist.nursescheduling.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hist.nursescheduling.domain.enumNm.Department;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final NurseRepository nurseRepository;
    private final ShiftRepository shiftRepository;
    private final NurseLeaveRequestRepository leaveRequestRepository;
    private final NotificationService notificationService;
    private final SolverManager<NurseSchedule, Long> solverManager;

    private final Map<String, NurseSchedule> solutionCache = new ConcurrentHashMap<>();

    private static final Set<Department> SHIFT_DEPARTMENTS = Set.of(
            Department.CTMICU, Department.CTSICU, Department.CTIICU,
            Department.CTNICU, Department.CTREMC, Department.MDEMER,
            Department.CTORRM, Department.MDHOSP, Department.CTHOSP, Department.CTCBMT);

    @Transactional
    public void startSolving(Department deptCode, int year, int month) {

        if (!SHIFT_DEPARTMENTS.contains(deptCode)) {
            log.info("부서 [{}]는 통상 근무 부서이므로 자동 배정에서 제외됩니다.", deptCode.getName());
            return;
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Nurse> nurseList = nurseRepository.findByDeptCode(deptCode).stream()
                .filter(n -> n.getWorkType() == Nurse.WorkType.SHIFT)
                .toList();
        List<NurseLeaveRequest> leaveRequestList = leaveRequestRepository
                .findByDeptCodeAndRequestedDateBetween(deptCode, start, end);
        List<Shift> shiftList = shiftRepository.findByDeptCode(deptCode.name());

        if (nurseList.isEmpty()) {
            log.warn("해당 부서[{}]에 3교대 근무자가 없어 배정을 중단합니다.", deptCode);
            return;
        }

        NurseSchedule problem = new NurseSchedule(nurseList, shiftList, leaveRequestList, null);
        Long problemId = (long) deptCode.ordinal();

        // 2. solveBuilder 스타일로 변경
        solverManager.solveBuilder()
                .withProblemId(problemId)
                .withProblemFinder(id -> problem)
                // [중간 과정] 최적해를 찾을 때마다 실행 (실시간 점수 전송)
                .withBestSolutionEventConsumer(event -> {
                    notificationService.sendScoreUpdate(deptCode.name(), event.solution().getScore().toString());
                })
                // [최종 결과] 계산이 완전히 끝났을 때 실행 (DB 저장)
                .withFinalBestSolutionEventConsumer(event -> {
                    solutionCache.put(deptCode.name(), event.solution());
                    log.info("부서 [{}] 배정 완료 및 캐시 저장", deptCode);
                })
                // 예외 처리
                .withExceptionHandler((id, exception) -> {
                    log.error("부서 [{}] 배정 중 에러 발생: ", deptCode, exception);
                })
                .run(); // 실행
    }

    @Transactional
    public void saveSolution(String deptCode) {
        NurseSchedule solution = solutionCache.get(deptCode);
        if (solution != null) {
            shiftRepository.saveAll(solution.getShiftList());
            solutionCache.remove(deptCode); // 저장 후 캐시 삭제
            log.info("부서 [{}] 배정 결과 DB 저장 완료", deptCode);
        } else {
            throw new IllegalArgumentException("저장할 배정 결과가 없습니다.");
        }
    }
}