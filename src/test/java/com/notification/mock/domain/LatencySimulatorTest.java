package com.notification.mock.domain;

import org.junit.jupiter.api.Test;

/**
 * LatencySimulator의 지수 분포가 실제로 어떻게 나오는지 확인하는 테스트입니다.
 * gradlew.bat test --tests "com.notification.mock.domain.LatencySimulatorTest"
 * -p c:\dev\tmp\NotificationMockServer
 */
class LatencySimulatorTest {

    @Test
    void 지수분포_지연시간_분포_확인() {
        int sampleSize = 10000;
        int under4s = 0;
        int between4and10s = 0;
        int between10and30s = 0;
        long totalMillis = 0;

        for (int i = 0; i < sampleSize; i++) {
            LatencySimulator simulator = new LatencySimulator();
            long delay = simulator.getDelayMillis();
            totalMillis += delay;

            System.out.println(delay);

            if (delay < 4000)
                under4s++;
            else if (delay < 10000)
                between4and10s++;
            else
                between10and30s++;
        }

        double avgSeconds = (totalMillis / (double) sampleSize) / 1000;

        System.out.println("=== 지수 분포 지연시간 테스트 (" + sampleSize + "건) ===");
        System.out.println();
        System.out.printf("0 ~ 4초  : %5d건 (%5.1f%%)%n", under4s, under4s * 100.0 / sampleSize);
        System.out.printf("4 ~ 10초 : %5d건 (%5.1f%%)%n", between4and10s, between4and10s * 100.0 / sampleSize);
        System.out.printf("10 ~ 30초: %5d건 (%5.1f%%)%n", between10and30s, between10and30s * 100.0 / sampleSize);
        System.out.println();
        System.out.printf("평균 응답시간: %.2f초%n", avgSeconds);
    }
}
