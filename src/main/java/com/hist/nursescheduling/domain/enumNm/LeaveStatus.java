package com.hist.nursescheduling.domain.enumNm;

public enum LeaveStatus {
    PENDING,  // 승인 대기
    APPROVED, // 승인 완료 (AI는 이 상태만 반영)
    REJECTED  // 반려
}
