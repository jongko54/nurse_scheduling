package com.hist.nursescheduling.comparator;

import com.hist.nursescheduling.domain.Shift;

import java.io.Serializable;
import java.util.Comparator;

/**
 * 어떤 근무(Shift)를 먼저 배정할지 결정하는 비교기입니다.
 * 시작 시간이 빠를수록 '어려운 근무'로 판단하여 솔버가 먼저 배정하도록 합니다.
 */
public class ShiftDifficultyComparator implements Comparator<Shift>, Serializable {

    @Override
    public int compare(Shift a, Shift b) {
        return Comparator.comparing(Shift::getStartDateTime) // 1. 시작 시간이 빠른 순서
                .thenComparing(Shift::getShiftType)          // 2. 근무 타입 순서
                .thenComparingLong(Shift::getId)             // 3. ID를 통한 고유성 보장 (필수)
                .compare(a, b);
    }
}