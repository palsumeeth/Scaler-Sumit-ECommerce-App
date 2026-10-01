package com.example.nxt.email_service.controller;

import com.example.nxt.email_service.entity.NotificationLog;
import com.example.nxt.email_service.repo.NotificationLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationController(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @GetMapping
    public List<NotificationLog> list() {
        return notificationLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
