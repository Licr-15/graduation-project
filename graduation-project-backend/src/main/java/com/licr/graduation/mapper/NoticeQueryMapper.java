package com.licr.graduation.mapper;

import com.licr.graduation.vo.Views;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NoticeQueryMapper {
    @Select("SELECT n.id,n.title,n.content,u.nickname AS adminName,n.created_at AS createdAt FROM sys_notice n JOIN `user` u ON u.id=n.admin_id WHERE n.status=1 ORDER BY n.created_at DESC")
    List<Views.Notice> list();
}
