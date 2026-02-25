package com.notification.mock.service;

import com.notification.mock.domain.DispatchResult;
import com.notification.mock.domain.DispatchStatus;
import com.notification.mock.domain.LatencySimulator;
import com.notification.mock.domain.NotificationChannel;
import com.notification.mock.exception.DispatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 알림 발송 시뮬레이션 서비스입니다.
 * 랜덤 지연과 랜덤 결과를 반환합니다.
 */
@Slf4j
@Service
public class NotificationDispatchService {

    /**
     * 주어진 채널로 알림 발송을 시뮬레이션합니다.
     *
     * @param channel 발송 채널 (EMAIL, TEAMS)
     * @return 발송 시뮬레이션 결과
     * @throws DispatchException ERROR 상태가 선택된 경우
     */
    public DispatchResult dispatch(NotificationChannel channel) {
        LatencySimulator latency = new LatencySimulator();
        DispatchStatus status = DispatchStatus.random();

        log.info("[{}] Dispatching notification — simulated latency: {}ms, status: {}",
                channel, latency.getDelayMillis(), status);

        // 랜덤으로 대기
        latency.simulate();

        // 에러인 경우 예외 발생
        if (status == DispatchStatus.ERROR) {
            throw new DispatchException(
                    String.format("[%s] Unexpected dispatch error occurred during notification delivery", channel));
        }

        return new DispatchResult(status, latency.getDelayMillis());
    }
}
