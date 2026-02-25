package com.notification.mock.exception;

/**
 * 알림 발송 중 시뮬레이션된 장애를 나타내는 예외입니다.
 */
public class DispatchException extends RuntimeException {

    public DispatchException(String message) {
        super(message);
    }

    public DispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
