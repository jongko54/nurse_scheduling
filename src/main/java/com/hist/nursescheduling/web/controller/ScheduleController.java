package com.hist.nursescheduling.web.controller;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.hist.nursescheduling.domain.NurseSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    @Autowired
    SolverManager<NurseSchedule, Long> solverManager;

    @PostMapping("/solve")
    public void solve(@RequestBody NurseSchedule problem) {
        solverManager.solveBuilder()
                .withProblemId(1L) // 문제 식별 ID
                .withProblemFinder(id -> problem) // 또는 바로 .withProblem(problem) 사용 가능
                .withBestSolutionConsumer(finalSchedule -> {
                    // 매번 새로운 최적해가 발견될 때마다 실행될 로직 (DB 저장 등)
                    System.out.println("새로운 최적해 발견! 점수: " + finalSchedule.solution().getScore());
                })
                .withExceptionHandler((problemId, throwable) -> {
                    // 에러 발생 시 처리 로직 (선택 사항)
                    throwable.printStackTrace();
                })
                .run(); // 솔버 시작
    }
}