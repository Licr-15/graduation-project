package com.licr.graduation.repository;

import com.licr.graduation.entity.UserEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<UserEntity> mapper = (r, n) -> new UserEntity(r.getLong("id"), r.getString("username"), r.getString("password"), r.getString("nickname"), r.getString("phone"), r.getString("avatar"), r.getString("role"), r.getInt("status"), r.getTimestamp("created_at").toLocalDateTime());
    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public Optional<UserEntity> findByUsername(String username) { return jdbc.query("SELECT * FROM `user` WHERE username=?", mapper, username).stream().findFirst(); }
    public Optional<UserEntity> findById(Long id) { return jdbc.query("SELECT * FROM `user` WHERE id=?", mapper, id).stream().findFirst(); }
    public long insert(String username, String password, String nickname) { jdbc.update("INSERT INTO `user`(username,password,nickname,role,status) VALUES(?,?,?,'STUDENT',1)", username,password,nickname); return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class); }
    public void updateProfile(Long id, String nickname, String phone, String avatar) { jdbc.update("UPDATE `user` SET nickname=?,phone=?,avatar=? WHERE id=?", nickname,phone,avatar,id); }
}
