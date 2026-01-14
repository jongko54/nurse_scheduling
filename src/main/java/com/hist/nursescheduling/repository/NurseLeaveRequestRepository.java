package com.hist.nursescheduling.repository;

import com.hist.nursescheduling.domain.Nurse;
import com.hist.nursescheduling.domain.NurseLeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NurseLeaveRequestRepository extends JpaRepository<NurseLeaveRequest, Long> {

    List<NurseLeaveRequest> findByNurseIn(List<Nurse> nurses);
}