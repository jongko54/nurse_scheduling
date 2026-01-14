package com.hist.nursescheduling.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name = "nurse_leave_request", catalog = "empinfo")
public class NurseLeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nurse_id")
    private Nurse nurse;

    private LocalDate requestedDate;
    private String deptCode;
    private String deptName;
    private String teamCode;
    private String teamNm;
}