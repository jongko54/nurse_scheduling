package com.hist.nursescheduling.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.hist.nursescheduling.domain.enumNm.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "nurse_leave_request", catalog = "empinfo")
public class NurseLeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @PlanningId
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id")
    private Nurse nurse; // 간호사 객체와 연결

    @Column(nullable = false)
    private LocalDate requestedDate; // 휴가 희망 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status = LeaveStatus.APPROVED; // 상태 (기본값: 승인)

    @Enumerated(EnumType.STRING)
    private LeaveType leaveType = LeaveType.ANNUAL; // 종류 (기본값: 연차)

    @Enumerated(EnumType.STRING)
    private Department deptCode; // MDINTM, CTNICU 등

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 신청 일시 (자동 생성)

    private String employeeNum; // 사번
    private String name; // 이름
}