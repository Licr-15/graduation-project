package com.licr.graduation.service;

import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.vo.Views;
import java.util.List;

public interface NoticeService {
    List<Views.Notice> list();
    Long create(CurrentUser admin,ActivityDtos.Notice input);
}
