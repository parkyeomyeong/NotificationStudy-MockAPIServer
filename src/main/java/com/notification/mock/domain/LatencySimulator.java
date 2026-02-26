package com.notification.mock.domain;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 외부 알림 채널 API의 응답 지연을 시뮬레이션합니다.
 * 지수 분포를 적용하여 실제 네트워크 지연 패턴을 모사합니다.
 * 대부분 1~5초 안에 응답하고, 간혹 10초 이상 걸리는 현실적인 분포를 만듭니다.
 */
public class LatencySimulator {

    private static final int MAX_LATENCY_SECONDS = 30;
    private static final double LAMBDA = 0.25; // 평균 = 1/λ = 4초

    private final long delayMillis;

    public LatencySimulator() {
        // 지수 분포: -ln(1-U) / λ (U는 0~1 균등 분포)
        double uniform = ThreadLocalRandom.current().nextDouble();
        double exponentialSeconds = -Math.log(1 - uniform) / LAMBDA;

        // 상한 30초로 제한
        long millis = (long) (Math.min(exponentialSeconds, MAX_LATENCY_SECONDS) * 1000);
        this.delayMillis = millis;
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
