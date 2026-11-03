package com.licr.graduation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.licr.graduation.persistence.ActivitySignRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface ActivitySignMapper extends BaseMapper<ActivitySignRow> {
    @Insert("INSERT INTO activity_sign(activity_id,user_id,status) VALUES(#{activityId},#{userId},'JOINED') ON DUPLICATE KEY UPDATE status='JOINED'")
    void join(@Param("activityId") Long activityId, @Param("userId") Long userId);
}
