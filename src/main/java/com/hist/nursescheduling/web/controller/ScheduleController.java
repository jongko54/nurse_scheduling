package com.hist.nursescheduling.web.controller;

import com.hist.nursescheduling.domain.Department;
import com.hist.nursescheduling.web.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/solve/{deptCode}")
    public ResponseEntity<String> solve(@PathVariable String deptCode) {

        scheduleService.startSolving(Department.valueOf(deptCode));

        return ResponseEntity.ok(deptCode + " 부서 배정 시작됨");
    }
}