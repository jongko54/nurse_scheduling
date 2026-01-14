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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성 (추천)
public class ScheduleService {

    private final NurseRepository nurseRepository;
    private final ShiftRepository shiftRepository;
    private final NurseLeaveRequestRepository leaveRequestRepository;
    private final NotificationService notificationService;
    private final SolverManager<NurseSchedule, Long> solverManager;

    @Transactional
    public void startSolving(Department deptCode) { // String 대신 Department Enum 사용

        List<Nurse> nurseList = nurseRepository.findByDeptCode(String.valueOf(deptCode));
        List<NurseLeaveRequest> leaveRequestList = leaveRequestRepository.findByNurseIn(nurseList);

        // Shift 엔티티의 필드명이 deptCode(String)로 변경되었다고 가정
        List<Shift> shiftList = shiftRepository.findByDeptCode(deptCode.name());

        // 2. Planning Solution 생성
        NurseSchedule problem = new NurseSchedule(
                nurseList,
                shiftList,
                leaveRequestList,
                null // score는 초기값 null
        );

        // 3. 솔버 실행 (비동기)
        // Enum의 ordinal()을 사용하여 고유한 Problem ID 생성
        Long problemId = (long) deptCode.ordinal();

        solverManager.solveBuilder()
                .withProblemId(problemId)
                .withProblemFinder(id -> problem)
                .withBestSolutionEventConsumer(event -> {
                    // 1. 이벤트에서 최적해(NurseSchedule) 추출
                    NurseSchedule bestSolution = event.solution();
                    var score = bestSolution.getScore();
                    log.info("부서 [{}] 최적화 진행 중... 현재 점수: {}", deptCode, score);
                    notificationService.sendScoreUpdate(deptCode.name(), score.toString());
                })
                .withExceptionHandler((id, exception) -> {
                    log.error("부서 [{}] 최적화 중 에러 발생: ", deptCode, exception);
                })
                .run();
    }

    @Transactional
    public void saveSolution(NurseSchedule solution) {
        // AI가 배정한 Nurse 정보가 담긴 Shift 리스트를 DB에 일괄 저장
        shiftRepository.saveAll(solution.getShiftList());
    }
}