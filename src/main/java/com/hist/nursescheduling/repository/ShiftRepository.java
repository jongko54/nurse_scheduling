package com.hist.nursescheduling.repository;

import com.hist.nursescheduling.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    // (권장) 특정 부서의 특정 기간 내 근무 슬롯만 조회
    List<Shift> findByDeptCodeAndStartDateTimeBetween(
            String deptCode,
            LocalDateTime start,
            LocalDateTime end
    );
    List<Shift> findByDeptCode(String name);
}