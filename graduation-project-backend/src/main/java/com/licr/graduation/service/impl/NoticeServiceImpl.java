package com.licr.graduation.service.impl;

import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.mapper.NoticeMapper;
import com.licr.graduation.mapper.NoticeQueryMapper;
import com.licr.graduation.persistence.SysNoticeRow;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.service.NoticeService;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service public class NoticeServiceImpl implements NoticeService {
    private final NoticeMapper mapper;private final NoticeQueryMapper queries;
    public NoticeServiceImpl(NoticeMapper mapper,NoticeQueryMapper queries){this.mapper=mapper;this.queries=queries;}
    public List<Views.Notice> list(){return queries.list();}
    @Transactional public Long create(CurrentUser admin,ActivityDtos.Notice input){SysNoticeRow row=new SysNoticeRow();row.title=input.title();row.content=input.content();row.adminId=admin.id();row.status=1;mapper.insert(row);return row.id;}
}
