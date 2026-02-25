package com.notification.mock.exception;

import com.notification.mock.api.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * 전역 예외 처리 핸들러입니다.
 * Controller에서 발생하는 예외를 일관된 응답 형식으로 변환합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DispatchException.class)
    public ResponseEntity<NotificationResponse> handleDispatchException(DispatchException ex) {
        log.error("Dispatch error: {}", ex.getMessage());

        NotificationResponse response = new NotificationResponse(
                "ERROR",
                ex.getMessage(),
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<NotificationResponse> handleGenericException(Exception ex) {
        log.error("Unexpected server error", ex);

        NotificationResponse response = new NotificationResponse(
                "ERROR",
                "Internal server error",
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
