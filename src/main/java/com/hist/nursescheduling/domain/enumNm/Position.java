package com.hist.nursescheduling.domain.enumNm;

public enum Position {
    HN("수간호사"),
    CN("책임간호사"),
    SN("일반간호사"),
    NJ("신입간호사");

    public boolean isSenior() {

        return this == HN || this == CN;
    }

    private final String name;

    Position(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    Position fromCode(String code) {
        for (Position positionCode : Position.values()) {
            if (positionCode.name().equals(code)) {
                return positionCode;
            }
        }
        return null;
    }
}