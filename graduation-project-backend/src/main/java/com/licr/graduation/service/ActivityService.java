package com.licr.graduation.service;

import com.licr.graduation.common.BusinessException;
import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.entity.ActivityEntity;
import com.licr.graduation.repository.ActivityRepository;
import com.licr.graduation.repository.VenueRepository;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {
    private final ActivityRepository activities; private final VenueRepository venues;
    public ActivityService(ActivityRepository activities,VenueRepository venues){this.activities=activities;this.venues=venues;}
    public List<Views.Activity> search(String keyword,String sportType,int page,int size){if(page<1||size<1||size>100)throw new BusinessException("分页参数不合法");return activities.search(keyword,sportType,page,size);}
    public List<Views.Activity> mine(CurrentUser user){return activities.mine(user.id());}
    @Transactional public Long create(CurrentUser user,ActivityDtos.Save in){validate(in);validateVenue(in.venueId());return activities.insert(user.id(),in.venueId(),in.title(),in.sportType(),in.description(),in.startTime(),in.endTime(),in.maxPeople());}
    @Transactional public void update(CurrentUser user,Long id,ActivityDtos.Save in){ActivityEntity old=owned(user,id);validate(in);validateVenue(in.venueId());activities.update(id,in.venueId(),in.title(),in.sportType(),in.description(),in.startTime(),in.endTime(),in.maxPeople());}
    @Transactional public void delete(CurrentUser user,Long id){owned(user,id);activities.cancel(id);}
    @Transactional public void join(CurrentUser user,Long id){ActivityEntity a=activities.find(id).orElseThrow(()->new BusinessException("活动不存在"));if(!"OPEN".equals(a.status()))throw new BusinessException("活动当前不可报名");if(a.publisherId().equals(user.id()))throw new BusinessException("不能报名自己发布的活动");int count=activities.lockAndCount(id);if(count>=a.maxPeople())throw new BusinessException("活动人数已满");activities.join(id,user.id());}
    @Transactional public void leave(CurrentUser user,Long id){ActivityEntity a=activities.find(id).orElseThrow(()->new BusinessException("活动不存在"));if(!"OPEN".equals(a.status()))throw new BusinessException("活动当前不可取消报名");activities.leave(id,user.id());}
    private ActivityEntity owned(CurrentUser u,Long id){ActivityEntity a=activities.find(id).orElseThrow(()->new BusinessException("活动不存在"));if(!a.publisherId().equals(u.id()))throw new BusinessException("只能操作自己发布的活动");return a;}
    private void validate(ActivityDtos.Save in){if(!in.endTime().isAfter(in.startTime()))throw new BusinessException("结束时间必须晚于开始时间");if(!in.startTime().isAfter(LocalDateTime.now()))throw new BusinessException("活动开始时间必须晚于当前时间");}
    private void validateVenue(Long id){if(id!=null&&venues.find(id).filter(v->v.status()==1).isEmpty())throw new BusinessException("场地不存在或已停用");}
}
