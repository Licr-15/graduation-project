package com.licr.graduation.service;

import java.util.List;
import java.util.Map;

public interface UserNotificationService {
    void create(long recipientId, Long actorId, String type, String title, String content, String link);
    List<Map<String, Object>> mine(long userId);
    int unreadCount(long userId);
    void markRead(long userId, long notificationId);
    void markAllRead(long userId);
}
