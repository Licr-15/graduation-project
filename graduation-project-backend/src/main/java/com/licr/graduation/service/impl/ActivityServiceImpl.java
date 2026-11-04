package com.licr.graduation.service.impl;

import com.licr.graduation.common.BusinessException;
import com.licr.graduation.dto.ActivityDtos;
import com.licr.graduation.entity.ActivityEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.licr.graduation.mapper.ActivityMapper;
import com.licr.graduation.mapper.ActivitySignMapper;
import com.licr.graduation.mapper.VenueMapper;
import com.licr.graduation.persistence.ActivityRow;
import com.licr.graduation.persistence.ActivitySignRow;
import com.licr.graduation.persistence.SportVenueRow;
import com.licr.graduation.security.CurrentUser;
import com.licr.graduation.service.ActivityService;
import com.licr.graduation.vo.Views;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {
    private final ActivityMapper activities; private final ActivitySignMapper signs; private final VenueMapper venues;
    public ActivityServiceImpl(ActivityMapper activities,ActivitySignMapper signs,VenueMapper venues){this.activities=activities;this.signs=signs;this.venues=venues;}
    public List<Views.Activity> search(String keyword,String sportType,int page,int size){if(page<1||size<1||size>100)throw new BusinessException("分页参数不合法");return activities.search(keyword,sportType,size,(page-1)*size);}
    public List<Views.Activity> mine(CurrentUser user){return activities.mine(user.id());}
    public List<Views.Activity> joined(CurrentUser user){return activities.joined(user.id());}
    public Views.Activity detail(Long id){Views.Activity activity=activities.detail(id);if(activity==null)throw new BusinessException("活动不存在");return activity;}
    @Transactional public Long create(CurrentUser user,ActivityDtos.Save in){validate(in);validateVenue(in.venueId());ActivityRow row=new ActivityRow();row.publisherId=user.id();row.venueId=in.venueId();row.title=in.title();row.sportType=in.sportType();row.description=in.description();row.startTime=in.startTime();row.endTime=in.endTime();row.maxPeople=in.maxPeople();row.status="OPEN";activities.insert(row);return row.id;}
    @Transactional public void update(CurrentUser user,Long id,ActivityDtos.Save in){owned(user,id);validate(in);validateVenue(in.venueId());activities.update(null,new UpdateWrapper<ActivityRow>().eq("id",id).set("venue_id",in.venueId()).set("title",in.title()).set("sport_type",in.sportType()).set("description",in.description()).set("start_time",in.startTime()).set("end_time",in.endTime()).set("max_people",in.maxPeople()));}
    @Transactional public void delete(CurrentUser user,Long id){owned(user,id);activities.update(null,new UpdateWrapper<ActivityRow>().eq("id",id).set("status","CANCELLED"));}
    @Transactional public void join(CurrentUser user,Long id){ActivityEntity a=find(id);if(!"OPEN".equals(a.status()))throw new BusinessException("活动当前不可报名");if(a.publisherId().equals(user.id()))throw new BusinessException("不能报名自己发布的活动");activities.lock(id);int count=Math.toIntExact(signs.selectCount(new QueryWrapper<ActivitySignRow>().eq("activity_id",id).eq("status","JOINED")));if(count>=a.maxPeople())throw new BusinessException("活动人数已满");signs.join(id,user.id());}
    @Transactional public void leave(CurrentUser user,Long id){ActivityEntity a=find(id);if(!"OPEN".equals(a.status()))throw new BusinessException("活动当前不可取消报名");signs.update(null,new UpdateWrapper<ActivitySignRow>().eq("activity_id",id).eq("user_id",user.id()).set("status","CANCELLED"));}
    private ActivityEntity owned(CurrentUser u,Long id){ActivityEntity a=find(id);if(!a.publisherId().equals(u.id()))throw new BusinessException("只能操作自己发布的活动");return a;}
    private ActivityEntity find(Long id){ActivityRow row=activities.selectById(id);if(row==null)throw new BusinessException("活动不存在");return new ActivityEntity(row.id,row.publisherId,row.venueId,row.title,row.sportType,row.description,row.startTime,row.endTime,row.maxPeople,row.status,row.createdAt);}
    private void validate(ActivityDtos.Save in){if(!in.endTime().isAfter(in.startTime()))throw new BusinessException("结束时间必须晚于开始时间");if(!in.startTime().isAfter(LocalDateTime.now()))throw new BusinessException("活动开始时间必须晚于当前时间");}
    private void validateVenue(Long id){if(id!=null){SportVenueRow venue=venues.selectById(id);if(venue==null||venue.status!=1)throw new BusinessException("场地不存在或已停用");}}
}
