package com.licr.graduation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.licr.graduation.persistence.UserRow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserRow> {}
