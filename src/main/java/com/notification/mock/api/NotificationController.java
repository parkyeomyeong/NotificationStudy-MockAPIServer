package com.notification.mock.api;

import com.notification.mock.api.dto.EmailRequest;
import com.notification.mock.api.dto.NotificationResponse;
import com.notification.mock.api.dto.TeamsRequest;
import com.notification.mock.domain.DispatchResult;
import com.notification.mock.domain.NotificationChannel;
import com.notification.mock.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

        private final NotificationDispatchService dispatchService;

        @PostMapping("/email")
        public ResponseEntity<NotificationResponse> sendEmail(@RequestBody EmailRequest request) {
                log.info("Email dispatch request received — to: {}, title: {}", request.toEmail(),
                                request.title());

                DispatchResult result = dispatchService.dispatch(NotificationChannel.EMAIL);

                return ResponseEntity.ok(new NotificationResponse(
                                result.status().getValue(),
                                String.format("Email dispatch completed in %dms", result.latencyMillis()),
                                LocalDateTime.now()));
        }

        @PostMapping("/teams")
        public ResponseEntity<NotificationResponse> sendTeams(@RequestBody TeamsRequest request) {
                log.info("Teams dispatch request received — userId: {}, title: {}", request.toTeamsUserId(),
                                request.title());

                DispatchResult result = dispatchService.dispatch(NotificationChannel.TEAMS);

                return ResponseEntity.ok(new NotificationResponse(
                                result.status().getValue(),
                                String.format("Teams dispatch completed in %dms", result.latencyMillis()),
                                LocalDateTime.now()));
        }
}
