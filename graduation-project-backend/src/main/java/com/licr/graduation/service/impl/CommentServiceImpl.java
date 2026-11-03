package com.licr.graduation.service.impl;

import com.licr.graduation.common.BusinessException;
import com.licr.graduation.dto.ActivityDtos;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.licr.graduation.mapper.ActivityMapper;
import com.licr.graduation.mapper.CommentMapper;
import com.licr.graduation.mapper.CommentQueryMapper;
import com.licr.graduation.persistence.ActivityRow;
import com.licr.graduation.persistence.CommentRow;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.service.CommentService;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service public class CommentServiceImpl implements CommentService {
    private final CommentMapper comments;private final CommentQueryMapper queries;private final ActivityMapper activities;
    public CommentServiceImpl(CommentMapper comments,CommentQueryMapper queries,ActivityMapper activities){this.comments=comments;this.queries=queries;this.activities=activities;}
    public List<Views.Comment> list(Long activityId){return queries.list(activityId);}
    @Transactional public Long create(CurrentUser u,Long activityId,ActivityDtos.Comment input){if(activities.selectById(activityId)==null)throw new BusinessException("活动不存在");CommentRow row=new CommentRow();row.activityId=activityId;row.userId=u.id();row.content=input.content();comments.insert(row);return row.id;}
    @Transactional public void delete(CurrentUser u,Long id){if(comments.selectCount(new QueryWrapper<CommentRow>().eq("id",id).eq("user_id",u.id()))==0)throw new BusinessException("只能删除自己的评论");comments.deleteById(id);}
}
