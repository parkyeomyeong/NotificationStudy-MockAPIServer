package com.notification.mock.api.dto;

import java.time.LocalDateTime;

/**
 * 알림 발송 응답 DTO.
 *
 * @param status    발송 결과 (SUCCESS / FAILURE / ERROR)
 * @param message   결과 설명 메시지
 * @param timestamp 응답 시각
 */
public record NotificationResponse(String status,String message,LocalDateTime timestamp){}
