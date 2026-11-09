package com.licr.graduation.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("activity")
public class ActivityRow {
    @TableId(type = IdType.AUTO) public Long id;
    public Long publisherId;
    public Long venueId;
    public String title;
    public String sportType;
    public String description;
    public String imageUrl;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public Integer maxPeople;
    public String status;
    public LocalDateTime createdAt;
}
