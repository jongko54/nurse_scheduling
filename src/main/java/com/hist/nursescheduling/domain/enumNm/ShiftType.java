package com.hist.nursescheduling.domain.enumNm;

public enum ShiftType {
    WARD_DAY("07:00", "15:30"),
    WARD_EVENING("15:00", "23:00"),
    WARD_NIGHT("22:30", "07:30"),
    OR_MAIN("08:00", "17:00"),
    OR_EVENING("13:00", "21:00"),
    OR_ONCALL("21:00", "08:00"),
    OUTPATIENT_AM("09:00", "18:00"),
    OFF("00:00", "00:00");

    private final String startTime;
    private final String endTime;

    ShiftType(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // 밤근무(N) 카테고리인지 확인하는 메서드
    public boolean isNight() {
        return this == WARD_NIGHT || this == OR_ONCALL;
    }

    // 낮/초번 근무(D/E) 카테고리인지 확인하는 메서드
    public boolean isDayOrEvening() {
        return this == WARD_DAY || this == WARD_EVENING || this == OR_MAIN || this == OR_EVENING || this == OUTPATIENT_AM;
    }
}
