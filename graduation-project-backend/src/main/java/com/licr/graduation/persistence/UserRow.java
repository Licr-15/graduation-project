package com.licr.graduation.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("user")
public class UserRow {
    @TableId(type = IdType.AUTO) public Long id;
    public String username;
    public String password;
    public String nickname;
    public String phone;
    public String avatar;
    public String role;
    public Integer status;
    public LocalDateTime createdAt;
}
