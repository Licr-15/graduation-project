package com.licr.graduation.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sport_venue")
public class SportVenueRow {
    @TableId(type = IdType.AUTO) public Long id;
    public String name;
    public String address;
    public String sportType;
    public String description;
    public Integer status;
    public LocalDateTime createdAt;
}
