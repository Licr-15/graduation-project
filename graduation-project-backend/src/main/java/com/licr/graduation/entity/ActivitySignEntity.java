package com.licr.graduation.entity;

import java.time.LocalDateTime;

/** 活动报名表实体，对应 activity_sign 表。 */
public record ActivitySignEntity(Long id, Long activityId, Long userId, String status, LocalDateTime createdAt) {}
