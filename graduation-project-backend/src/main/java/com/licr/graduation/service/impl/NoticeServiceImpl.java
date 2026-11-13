package com.licr.graduation.service.impl;

import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.mapper.NoticeQueryMapper;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.service.NoticeService;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

@Service public class NoticeServiceImpl implements NoticeService {
    private final JdbcTemplate db;private final NoticeQueryMapper queries;
    public NoticeServiceImpl(JdbcTemplate db,NoticeQueryMapper queries){this.db=db;this.queries=queries;}
    public List<Views.Notice> list(){return queries.list();}
    @Transactional public Long create(CurrentUser admin,ActivityDtos.Notice input){db.update("INSERT INTO community_post(user_id,post_type,title,content) VALUES(?,'NOTICE',?,?)",admin.id(),input.title(),input.content());return db.queryForObject("SELECT LAST_INSERT_ID()",Long.class);}
}
