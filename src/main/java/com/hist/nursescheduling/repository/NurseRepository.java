package com.hist.nursescheduling.repository;

import com.hist.nursescheduling.domain.enumNm.Department;
import com.hist.nursescheduling.domain.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NurseRepository extends JpaRepository<Nurse, Long> {

    List<Nurse> findByDeptCode(Department deptCode);

}
