package com.licr.graduation.controller;

import com.licr.graduation.common.Result;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.service.UserNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final UserNotificationService notifications;
    public NotificationController(UserNotificationService notifications) { this.notifications = notifications; }
    private CurrentUser user(HttpServletRequest request) { return AuthController.user(request); }

    @GetMapping("/mine")
    public Result<?> mine(HttpServletRequest request) { return Result.ok(notifications.mine(user(request).id())); }

    @GetMapping("/unread-count")
    public Result<?> unreadCount(HttpServletRequest request) { return Result.ok(Map.of("unreadCount", notifications.unreadCount(user(request).id()))); }

    @PutMapping("/{id}/read")
    public Result<?> read(HttpServletRequest request, @PathVariable long id) {
        notifications.markRead(user(request).id(), id);
        return Result.ok(null);
    }

    @PutMapping("/read-all")
    public Result<?> readAll(HttpServletRequest request) {
        notifications.markAllRead(user(request).id());
        return Result.ok(null);
    }
}
