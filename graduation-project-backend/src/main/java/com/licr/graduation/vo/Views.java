package com.licr.graduation.vo;

import java.time.LocalDateTime;

public final class Views {
    private Views() {}
    public record User(Long id, String username, String nickname, String phone, String avatar, String role) {}
    public record Login(String token, User user) {}
    public record Venue(Long id, String name, String address, String sportType, String description) {}
    public record Activity(Long id, Long publisherId, String publisherName, Long venueId, String venueName, String title, String sportType, String description, LocalDateTime startTime, LocalDateTime endTime, int maxPeople, int joinedCount, String status, LocalDateTime createdAt) {}
    public record Comment(Long id, Long activityId, Long userId, String nickname, String content, LocalDateTime createdAt) {}
    public record Notice(Long id, String title, String content, String adminName, LocalDateTime createdAt) {}
}
