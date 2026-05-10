package com.socialstack.controller;

import com.socialstack.dto.NotificationDto;
import com.socialstack.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> list(Authentication auth) {
        if (auth == null || auth.getDetails() == null || !(auth.getDetails() instanceof Long)) {
            return ResponseEntity.ok(List.of());
        }
        Long userId = (Long) auth.getDetails();
        boolean student = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_STUDENT"::equals);
        return ResponseEntity.ok(notificationService.buildNotificationsForUser(userId, student));
    }
}
