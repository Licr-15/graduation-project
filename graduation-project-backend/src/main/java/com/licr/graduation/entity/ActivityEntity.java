package com.licr.graduation.entity;

import java.time.LocalDateTime;

/** 运动活动表实体，对应 activity 表。 */
public record ActivityEntity(Long id, Long publisherId, Long venueId, String title, String sportType, String description, LocalDateTime startTime, LocalDateTime endTime, Integer maxPeople, String status, LocalDateTime createdAt) {}
