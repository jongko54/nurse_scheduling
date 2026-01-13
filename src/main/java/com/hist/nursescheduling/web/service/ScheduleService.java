package com.hist.nursescheduling.web.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.hist.nursescheduling.domain.NurseSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private SolverManager<NurseSchedule, String> solverManager; // ID를 부서명(String)으로 사용

    public void solveAllDepartments(List<String> departments) {
        for (String deptName : departments) {
            // 1. 해당 부서의 데이터만 추출 (DB에서 조회)
            NurseSchedule deptProblem = loadDataForDepartment(deptName);

            // 2. 부서별로 독립적인 솔버 작업 시작
            solverManager.solveBuilder()
                    .withProblemId(deptName) // 부서명을 고유 ID로 사용
                    .withProblem(deptProblem)
                    .withFinalBestSolutionConsumer(result -> {
                        // 3. 해당 부서의 계산이 끝나면 개별적으로 DB 저장
                        saveDepartmentResult(deptName, result.solution());
                    })
                    .run(); //
        }
    }
}