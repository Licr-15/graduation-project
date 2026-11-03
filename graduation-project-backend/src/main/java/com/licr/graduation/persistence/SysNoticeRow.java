package com.licr.graduation.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sys_notice")
public class SysNoticeRow {
    @TableId(type = IdType.AUTO) public Long id;
    public String title;
    public String content;
    public Long adminId;
    public Integer status;
    public LocalDateTime createdAt;
}
