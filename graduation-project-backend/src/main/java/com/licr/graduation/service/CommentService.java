package com.licr.graduation.service;

import com.licr.graduation.common.BusinessException;
import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.repository.ActivityRepository;
import com.licr.graduation.repository.CommentRepository;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service public class CommentService {
    private final CommentRepository comments;private final ActivityRepository activities;
    public CommentService(CommentRepository comments,ActivityRepository activities){this.comments=comments;this.activities=activities;}
    public List<Views.Comment> list(Long activityId){return comments.list(activityId);}
    @Transactional public Long create(CurrentUser u,Long activityId,ActivityDtos.Comment input){if(activities.find(activityId).isEmpty())throw new BusinessException("活动不存在");return comments.insert(activityId,u.id(),input.content());}
    @Transactional public void delete(CurrentUser u,Long id){if(!comments.owned(id,u.id()))throw new BusinessException("只能删除自己的评论");comments.delete(id);}
}
