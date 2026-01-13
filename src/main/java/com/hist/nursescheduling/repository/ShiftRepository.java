package com.hist.nursescheduling.repository;

import com.hist.nursescheduling.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long> {


}
