package com.licr.graduation.entity;

import java.time.LocalDateTime;

/** 系统公告表实体，对应 sys_notice 表。 */
public record SysNoticeEntity(Long id, String title, String content, Long adminId, Integer status, LocalDateTime createdAt) {}
