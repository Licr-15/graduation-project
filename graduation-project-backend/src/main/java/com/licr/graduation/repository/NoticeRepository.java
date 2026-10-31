package com.licr.graduation.repository;

import com.licr.graduation.vo.Views;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class NoticeRepository {
    private final JdbcTemplate jdbc;
    public NoticeRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public List<Views.Notice> list() { return jdbc.query("SELECT n.*,u.nickname admin_name FROM sys_notice n JOIN `user` u ON u.id=n.admin_id WHERE n.status=1 ORDER BY n.created_at DESC",(r,n)->new Views.Notice(r.getLong("id"),r.getString("title"),r.getString("content"),r.getString("admin_name"),r.getTimestamp("created_at").toLocalDateTime())); }
    public long insert(String title,String content,Long adminId) { jdbc.update("INSERT INTO sys_notice(title,content,admin_id) VALUES(?,?,?)",title,content,adminId);return jdbc.queryForObject("SELECT LAST_INSERT_ID()",Long.class); }
}
