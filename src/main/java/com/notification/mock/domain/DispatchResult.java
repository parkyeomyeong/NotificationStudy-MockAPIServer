package com.notification.mock.domain;

/**
 * 발송 시뮬레이션 결과를 담는 Value Object입니다.
 */
public record DispatchResult(DispatchStatus status, long latencyMillis) {
}
