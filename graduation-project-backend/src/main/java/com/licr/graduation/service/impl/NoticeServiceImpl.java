package com.licr.graduation.service.impl;

import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.mapper.NoticeQueryMapper;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.service.NoticeService;
import com.licr.graduation.service.UserNotificationService;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

@Service public class NoticeServiceImpl implements NoticeService {
    private final JdbcTemplate db;private final NoticeQueryMapper queries;private final UserNotificationService notifications;
    public NoticeServiceImpl(JdbcTemplate db,NoticeQueryMapper queries,UserNotificationService notifications){this.db=db;this.queries=queries;this.notifications=notifications;}
    public List<Views.Notice> list(){return queries.list();}
    @Transactional public Long create(CurrentUser admin,ActivityDtos.Notice input){db.update("INSERT INTO community_post(user_id,post_type,title,content) VALUES(?,'NOTICE',?,?)",admin.id(),input.title(),input.content());Long id=db.queryForObject("SELECT LAST_INSERT_ID()",Long.class);db.query("SELECT id FROM `user` WHERE status=1 AND role='STUDENT'",rs->{notifications.create(rs.getLong(1),admin.id(),"NOTICE","新的校园公告",input.title(),"/notices");});return id;}
}
