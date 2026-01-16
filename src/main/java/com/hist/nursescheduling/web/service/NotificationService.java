package com.hist.nursescheduling.web.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    // 부서별로 SseEmitter를 관리하기 위한 맵
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String deptCode) {
        // 만료 시간 30분 설정
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.put(deptCode, emitter);

        emitter.onCompletion(() -> emitters.remove(deptCode));
        emitter.onTimeout(() -> emitters.remove(deptCode));

        try {
            // 연결 성공 메시지 전송
            emitter.send(SseEmitter.event().name("connect").data("Connected to " + deptCode));
        } catch (IOException e) {
            emitters.remove(deptCode);
        }
        return emitter;
    }

    public void sendScoreUpdate(String deptCode, String score) {
        SseEmitter emitter = emitters.get(deptCode);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("scoreUpdate").data(score));
            } catch (IOException e) {
                emitters.remove(deptCode);
            }
        }
    }
}