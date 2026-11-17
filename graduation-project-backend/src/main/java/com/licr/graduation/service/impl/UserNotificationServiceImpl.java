package com.licr.graduation.service.impl;

import com.licr.graduation.service.UserNotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UserNotificationServiceImpl implements UserNotificationService {
    private final JdbcTemplate db;
    public UserNotificationServiceImpl(JdbcTemplate db) { this.db = db; }

    @Override
    public void create(long recipientId, Long actorId, String type, String title, String content, String link) {
        if (actorId != null && actorId == recipientId) return;
        db.update("INSERT INTO notification(recipient_id,actor_id,notification_type,title,content,link) VALUES(?,?,?,?,?,?)",
            recipientId, actorId, type, title, content, link);
    }

    @Override
    public List<Map<String, Object>> mine(long userId) {
        return db.queryForList("""
            SELECT n.id,n.notification_type AS type,n.title,n.content,n.link,n.read_at AS readAt,
              n.created_at AS createdAt,u.nickname AS actorNickname,u.avatar AS actorAvatar
            FROM notification n LEFT JOIN `user` u ON u.id=n.actor_id
            WHERE n.recipient_id=? ORDER BY n.created_at DESC,n.id DESC LIMIT 100
            """, userId);
    }

    @Override
    public int unreadCount(long userId) {
        Integer count = db.queryForObject("SELECT COUNT(*) FROM notification WHERE recipient_id=? AND read_at IS NULL", Integer.class, userId);
        return count == null ? 0 : count;
    }

    @Override
    @Transactional
    public void markRead(long userId, long notificationId) {
        db.update("UPDATE notification SET read_at=COALESCE(read_at,NOW()) WHERE id=? AND recipient_id=?", notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllRead(long userId) {
        db.update("UPDATE notification SET read_at=NOW() WHERE recipient_id=? AND read_at IS NULL", userId);
    }
}
