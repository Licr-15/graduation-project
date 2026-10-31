package com.licr.graduation.repository;

import com.licr.graduation.vo.Views;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CommentRepository {
    private final JdbcTemplate jdbc;
    public CommentRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public List<Views.Comment> list(Long activityId) { return jdbc.query("SELECT c.*,u.nickname FROM `comment` c JOIN `user` u ON u.id=c.user_id WHERE c.activity_id=? ORDER BY c.created_at ASC",(r,n)->new Views.Comment(r.getLong("id"),r.getLong("activity_id"),r.getLong("user_id"),r.getString("nickname"),r.getString("content"),r.getTimestamp("created_at").toLocalDateTime()),activityId); }
    public long insert(Long activityId,Long userId,String content) { jdbc.update("INSERT INTO `comment`(activity_id,user_id,content) VALUES(?,?,?)",activityId,userId,content);return jdbc.queryForObject("SELECT LAST_INSERT_ID()",Long.class); }
    public boolean owned(Long id,Long userId) { return jdbc.queryForObject("SELECT COUNT(*) FROM `comment` WHERE id=? AND user_id=?",Integer.class,id,userId)>0; }
    public void delete(Long id) { jdbc.update("DELETE FROM `comment` WHERE id=?",id); }
}
