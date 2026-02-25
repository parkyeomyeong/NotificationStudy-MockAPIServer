package com.notification.mock.api.dto;

/**
 * Teams 메시지 발송 요청 DTO입니다.
 */
public record TeamsRequest(
        String toTeamsUserId,
        String title,
        String content) {
}
