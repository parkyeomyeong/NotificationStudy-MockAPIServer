package com.notification.mock.domain;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 외부 알림 채널 API의 응답 지연을 시뮬레이션합니다.
 * 0~30초 범위의 랜덤 지연을 적용합니다. (클라우드 LB 타임아웃 30~60초 고려)
 */
public class LatencySimulator {

    private static final int MAX_LATENCY_SECONDS = 30;

    private final long delayMillis;

    public LatencySimulator() {
        this.delayMillis = ThreadLocalRandom.current().nextLong(MAX_LATENCY_SECONDS * 1000L + 1);
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    /**
     * 설정된 지연 시간만큼 현재 스레드를 대기시킵니다.
     */
    public void simulate() {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Latency simulation interrupted", e);
        }
    }
}
