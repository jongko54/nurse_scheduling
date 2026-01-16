package com.hist.nursescheduling.web.controller;

import com.hist.nursescheduling.domain.enumNm.Department;
import com.hist.nursescheduling.web.service.NotificationService;
import com.hist.nursescheduling.web.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleApiController {

    private final ScheduleService scheduleService;
    private final NotificationService notificationService;

    @PostMapping("/solve/{deptCode}")
    @ResponseBody // API 형태로 응답하기 위해 추가
    public ResponseEntity<String> solve(
            @PathVariable Department deptCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        LocalDate now = LocalDate.now();
        int y = (year != null) ? year : now.getYear();
        int m = (month != null) ? month : now.getMonthValue();

        scheduleService.startSolving(deptCode, y, m);

        return ResponseEntity.ok(deptCode + " 부서 배정 시작됨");
    }

    // SSE 구독 API (HTML에서 404가 났던 부분)
    @GetMapping(value = "/subscribe/{deptCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String deptCode) {
        return notificationService.subscribe(deptCode);
    }

    @PostMapping("/save/{deptCode}")
    public ResponseEntity<String> save(@PathVariable String deptCode) {
        try {
            scheduleService.saveSolution(deptCode);
            return ResponseEntity.ok("저장에 성공했습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("저장 실패: " + e.getMessage());
        }
    }
}