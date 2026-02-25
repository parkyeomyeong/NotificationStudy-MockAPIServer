package com.notification.mock.api.dto;

/**
 * 이메일 발송 요청 DTO입니다.
 */
public record EmailRequest(
        String toEmail,
        String title,
        String content) {
}
