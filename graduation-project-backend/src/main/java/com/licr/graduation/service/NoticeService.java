package com.licr.graduation.service;

import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.repository.NoticeRepository;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service public class NoticeService {
    private final NoticeRepository repo;
    public NoticeService(NoticeRepository repo){this.repo=repo;}
    public List<Views.Notice> list(){return repo.list();}
    @Transactional public Long create(CurrentUser admin,ActivityDtos.Notice input){return repo.insert(input.title(),input.content(),admin.id());}
}
