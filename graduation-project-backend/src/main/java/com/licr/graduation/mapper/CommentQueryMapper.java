package com.licr.graduation.mapper;

import com.licr.graduation.vo.Views;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentQueryMapper {
    @Select("SELECT c.id,c.activity_id AS activityId,c.user_id AS userId,u.nickname,c.content,c.created_at AS createdAt FROM `comment` c JOIN `user` u ON u.id=c.user_id WHERE c.activity_id=#{activityId} ORDER BY c.created_at ASC")
    List<Views.Comment> list(@Param("activityId") Long activityId);
}
