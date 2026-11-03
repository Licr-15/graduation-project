package com.licr.graduation.service;

import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.vo.Views;
import java.util.List;

public interface CommentService {
    List<Views.Comment> list(Long activityId);
    Long create(CurrentUser user,Long activityId,ActivityDtos.Comment input);
    void delete(CurrentUser user,Long id);
}
