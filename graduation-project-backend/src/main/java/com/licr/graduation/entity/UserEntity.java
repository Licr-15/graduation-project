package com.licr.graduation.entity;

import java.time.LocalDateTime;

/** 用户表实体，对应 user 表。 */
public record UserEntity(Long id, String username, String password, String nickname, String phone, String avatar, String role, Integer status, LocalDateTime createdAt) {}
