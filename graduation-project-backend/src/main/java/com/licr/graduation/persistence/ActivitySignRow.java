package com.licr.graduation.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("activity_sign")
public class ActivitySignRow {
    @TableId(type = IdType.AUTO) public Long id;
    public Long activityId;
    public Long userId;
    public String status;
    public LocalDateTime createdAt;
}
