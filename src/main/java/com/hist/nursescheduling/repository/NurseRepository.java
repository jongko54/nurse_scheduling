package com.hist.nursescheduling.repository;

import com.hist.nursescheduling.domain.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NurseRepository extends JpaRepository<Nurse, Long> {


}
