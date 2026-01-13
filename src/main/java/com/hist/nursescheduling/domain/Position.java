package com.hist.nursescheduling.domain;

public enum Position {
    HN, CN, SN, NJ; // 수간호사, 책임간호사, 일반간호사, 신입간호사

    public boolean isSenior() {

        return this == HN || this == CN;
    }
}