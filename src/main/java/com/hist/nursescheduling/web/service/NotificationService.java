package com.hist.nursescheduling.web.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    // 부서별 SSE 연결 관리
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String deptCode) {
        SseEmitter emitter = new SseEmitter(60 * 1000L); // 1분 유지
        emitters.put(deptCode, emitter);

        emitter.onCompletion(() -> emitters.remove(deptCode));
        emitter.onTimeout(() -> emitters.remove(deptCode));

        return emitter;
    }

    public void sendScoreUpdate(String deptCode, String score) {
        if (emitters.containsKey(deptCode)) {
            try {
                emitters.get(deptCode).send(SseEmitter.event()
                        .name("scoreUpdate")
                        .data(score));
            } catch (IOException e) {
                emitters.remove(deptCode);
            }
        }
    }
}
