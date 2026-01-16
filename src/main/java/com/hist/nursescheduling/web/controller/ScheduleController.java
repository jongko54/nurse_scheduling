package com.hist.nursescheduling.web.controller;

import com.hist.nursescheduling.domain.Shift;
import com.hist.nursescheduling.domain.enumNm.Department;
import com.hist.nursescheduling.domain.Nurse;
import com.hist.nursescheduling.domain.NurseLeaveRequest;
import com.hist.nursescheduling.repository.NurseLeaveRequestRepository;
import com.hist.nursescheduling.repository.NurseRepository;
import com.hist.nursescheduling.repository.ShiftRepository;
import com.hist.nursescheduling.web.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/schedule") // 기본 뷰 주소
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final NurseRepository nurseRepository;
    private final NurseLeaveRequestRepository nurseLeaveRequestRepository;
    private final ShiftRepository shiftRepository;

    @GetMapping("")
    public String index() {
        return "redirect:/schedule/MDINTM";
    }

    @GetMapping("/{deptCode}")
    public String getSchedulePage(
            @PathVariable Department deptCode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {

        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        LocalDate start = LocalDate.of(targetYear, targetMonth, 1);
        int daysInMonth = start.lengthOfMonth();

        List<Shift> assignedShifts = shiftRepository.findByDeptCode(deptCode.name());

        // Key: 간호사ID_날짜, Value: 근무타입(D, E, N)
        Map<String, String> shiftMap = assignedShifts.stream()
                .filter(s -> s.getNurse() != null)
                .collect(Collectors.toMap(
                        s -> s.getNurse().getId() + "_" + s.getStartDateTime().getDayOfMonth(),
                        s -> {
                            String type = s.getShiftType().name();
                            if (type.contains("DAY")) return "D";
                            if (type.contains("EVENING")) return "E";
                            if (type.contains("NIGHT")) return "N";
                            return "O";
                        },
                        (existing, replacement) -> existing // 중복 시 첫 번째 값 유지
                ));

        List<Nurse> nurseList = nurseRepository.findByDeptCode(deptCode);
        List<NurseLeaveRequest> leaveRequests = nurseLeaveRequestRepository
                .findByDeptCodeAndRequestedDateBetween(deptCode, start, start.withDayOfMonth(daysInMonth));

        Set<String> offSet = leaveRequests.stream()
                .map(req -> req.getNurse().getId() + "_" + req.getRequestedDate().getDayOfMonth())
                .collect(Collectors.toSet());

        model.addAttribute("deptCode", deptCode.name());
        model.addAttribute("nurseList", nurseList);
        model.addAttribute("offSet", offSet);
        model.addAttribute("daysInMonth", daysInMonth);
        model.addAttribute("targetYear", targetYear);
        model.addAttribute("targetMonth", targetMonth);
        model.addAttribute("shiftMap", shiftMap);

        return "schedule";
    }
}