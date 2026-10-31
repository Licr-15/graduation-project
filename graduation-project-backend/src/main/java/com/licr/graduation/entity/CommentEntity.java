package com.licr.graduation.entity;

import java.time.LocalDateTime;

/** 活动评论表实体，对应 comment 表。 */
public record CommentEntity(Long id, Long activityId, Long userId, String content, LocalDateTime createdAt) {}
