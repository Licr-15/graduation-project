package com.licr.graduation.entity;

import java.time.LocalDateTime;

/** 运动场地表实体，对应 sport_venue 表。 */
public record SportVenueEntity(Long id, String name, String address, String sportType, String description, Integer status, LocalDateTime createdAt) {}
