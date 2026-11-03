package com.licr.graduation.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("comment")
public class CommentRow {
    @TableId(type = IdType.AUTO) public Long id;
    public Long activityId;
    public Long userId;
    public String content;
    public LocalDateTime createdAt;
}
