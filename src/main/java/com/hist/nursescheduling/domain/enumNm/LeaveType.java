package com.hist.nursescheduling.domain.enumNm;

public enum LeaveType {
    ANNUAL("연차"),
    SICK("병가"),
    OFF_REQUEST("오프신청"),
    CONGRATULATORY("경조사");

    private final String description;
    LeaveType(String description) { this.description = description; }
}
